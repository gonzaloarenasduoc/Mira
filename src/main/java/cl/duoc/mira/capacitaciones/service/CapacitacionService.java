package cl.duoc.mira.capacitaciones.service;

import cl.duoc.mira.capacitaciones.dto.AgendarCapacitacionRequest;
import cl.duoc.mira.capacitaciones.dto.CalendarioMes;
import cl.duoc.mira.capacitaciones.dto.CapacitacionResponse;
import cl.duoc.mira.capacitaciones.dto.DetalleCapacitacionResponse;
import cl.duoc.mira.capacitaciones.dto.MarcarRealizadaRequest;
import cl.duoc.mira.capacitaciones.entity.Capacitacion;
import cl.duoc.mira.capacitaciones.entity.EstadoCapacitacion;
import cl.duoc.mira.capacitaciones.entity.Tecnico;
import cl.duoc.mira.capacitaciones.repository.CapacitacionRepository;
import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.operaciones.service.VentaService;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Capacitaciones de las operaciones (E007). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CapacitacionService {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final CapacitacionRepository capacitacionRepository;
    private final TecnicoService tecnicoService;
    private final EmpresaRepository empresaRepository;
    private final ArriendoService arriendoService;
    private final VentaService ventaService;
    private final EquipoService equipoService;
    private final Clock clock;

    /** HU-20: agenda una capacitación vinculada al arriendo que la origina. */
    @Transactional
    public CapacitacionResponse agendarParaArriendo(Long empresaId, Long arriendoId, AgendarCapacitacionRequest request) {
        if (!arriendoService.existe(empresaId, arriendoId)) {
            throw new RecursoNoEncontradoException("El arriendo");
        }
        return agendar(empresaId, arriendoId, request);
    }

    /** HU-20: agenda una capacitación vinculada a la venta que la origina. */
    @Transactional
    public CapacitacionResponse agendarParaVenta(Long empresaId, Long ventaId, AgendarCapacitacionRequest request) {
        if (!ventaService.existe(empresaId, ventaId)) {
            throw new RecursoNoEncontradoException("La venta");
        }
        return agendar(empresaId, ventaId, request);
    }

    /** Capacitaciones de una operación, de la más próxima a la más lejana. */
    public List<CapacitacionResponse> deOperacion(Long empresaId, Long operacionId) {
        return capacitacionRepository.findByEmpresaIdAndOperacionIdOrderByFechaProgramada(empresaId, operacionId).stream()
                .map(CapacitacionService::aResponse).toList();
    }

    /**
     * HU-20: se sugiere agendar cuando algún equipo de la operación es de una categoría que requiere
     * capacitación y la operación todavía no tiene una programada o realizada.
     */
    public boolean sugerirAgendar(Long empresaId, Long operacionId, Collection<Long> equipos) {
        boolean tieneCapacitacion = capacitacionRepository
                .findByEmpresaIdAndOperacionIdOrderByFechaProgramada(empresaId, operacionId).stream()
                .anyMatch(c -> c.getEstado() != EstadoCapacitacion.CANCELADA);
        return !tieneCapacitacion && equipoService.requierenCapacitacion(empresaId, equipos);
    }

    /**
     * HU-22: calendario del mes en semanas de lunes a domingo, con las capacitaciones de cada día en
     * cualquier estado. Los días de los meses vecinos que completan la grilla también las muestran.
     */
    public CalendarioMes calendario(Long empresaId, YearMonth mes) {
        LocalDate desde = mes.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate hasta = mes.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<DetalleCapacitacionResponse> capacitaciones = aDetalle(empresaId,
                capacitacionRepository.entre(empresaId, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()));
        Map<LocalDate, List<DetalleCapacitacionResponse>> porDia = capacitaciones.stream()
                .collect(Collectors.groupingBy(c -> c.fechaProgramada().toLocalDate()));
        LocalDate hoy = LocalDate.now(clock);
        List<List<CalendarioMes.Dia>> semanas = new ArrayList<>();
        for (LocalDate lunes = desde; !lunes.isAfter(hasta); lunes = lunes.plusWeeks(1)) {
            List<CalendarioMes.Dia> semana = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate dia = lunes.plusDays(i);
                semana.add(new CalendarioMes.Dia(dia, YearMonth.from(dia).equals(mes), dia.equals(hoy),
                        porDia.getOrDefault(dia, List.of())));
            }
            semanas.add(semana);
        }
        int delMes = (int) capacitaciones.stream().filter(c -> YearMonth.from(c.fechaProgramada()).equals(mes)).count();
        return new CalendarioMes(mes, semanas, delMes);
    }

    /** HU-22: ficha de una capacitación. Una de otra empresa se responde como inexistente (PNF-06). */
    public DetalleCapacitacionResponse detalle(Long empresaId, Long id) {
        return aDetalle(empresaId, List.of(obtener(empresaId, id))).getFirst();
    }

    /**
     * HU-22: la capacitación programada queda realizada en la fecha y hora indicadas, que no pueden ser
     * futuras. Realizada y cancelada son estados finales.
     */
    @Transactional
    public DetalleCapacitacionResponse marcarRealizada(Long empresaId, Long id, MarcarRealizadaRequest request) {
        Capacitacion capacitacion = programada(empresaId, id);
        LocalDateTime realizada = request.getFecha().atTime(request.getHora());
        if (realizada.isAfter(LocalDateTime.now(clock))) {
            throw new NegocioException("La fecha y hora en que se realizó no pueden ser futuras.");
        }
        capacitacion.marcarRealizada(realizada);
        return detalle(empresaId, id);
    }

    /** HU-22: la capacitación programada queda cancelada. Para hacerla después, se agenda una nueva. */
    @Transactional
    public DetalleCapacitacionResponse cancelar(Long empresaId, Long id) {
        programada(empresaId, id).cancelar();
        return detalle(empresaId, id);
    }

    private Capacitacion obtener(Long empresaId, Long id) {
        return capacitacionRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La capacitación"));
    }

    private Capacitacion programada(Long empresaId, Long id) {
        Capacitacion capacitacion = obtener(empresaId, id);
        if (capacitacion.getEstado() != EstadoCapacitacion.PROGRAMADA) {
            throw new NegocioException("La capacitación ya está "
                    + capacitacion.getEstado().getEtiqueta().toLowerCase(Locale.ROOT) + ": no admite cambios.");
        }
        return capacitacion;
    }

    /** Agrega el tipo y el cliente de cada operación con una consulta por tipo (sin N+1). */
    private List<DetalleCapacitacionResponse> aDetalle(Long empresaId, List<Capacitacion> capacitaciones) {
        Set<Long> operaciones = capacitaciones.stream().map(Capacitacion::getOperacionId).collect(Collectors.toSet());
        Map<Long, String> arriendos = arriendoService.clientesPorId(empresaId, operaciones);
        Map<Long, String> ventas = ventaService.clientesPorId(empresaId, operaciones);
        return capacitaciones.stream().map(c -> {
            boolean esVenta = ventas.containsKey(c.getOperacionId());
            String cliente = esVenta ? ventas.get(c.getOperacionId()) : arriendos.getOrDefault(c.getOperacionId(), "—");
            return new DetalleCapacitacionResponse(c.getId(), c.getOperacionId(), esVenta ? "Venta" : "Arriendo", cliente,
                    c.getTecnico().getNombreCompleto(), c.getFechaProgramada(), c.getFechaRealizada(), c.getLugar(),
                    c.getEstado(), c.getObservaciones());
        }).toList();
    }

    /**
     * HU-21 (CF-12): advierte si el técnico ya tiene otra capacitación programada ese día. Devuelve
     * nulo si no la tiene; las realizadas y las canceladas no cuentan.
     */
    public String avisoConflicto(Long empresaId, Long tecnicoId, LocalDate fecha) {
        List<Capacitacion> programadas = capacitacionRepository.delTecnicoEntre(empresaId, tecnicoId,
                EstadoCapacitacion.PROGRAMADA, fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
        if (programadas.isEmpty()) {
            return null;
        }
        String detalle = programadas.stream()
                .map(c -> "a las " + HORA.format(c.getFechaProgramada()) + " en " + c.getLugar())
                .collect(Collectors.joining("; "));
        return programadas.getFirst().getTecnico().getNombreCompleto() + " ya tiene "
                + (programadas.size() == 1 ? "una capacitación programada" : programadas.size() + " capacitaciones programadas")
                + " el " + DIA.format(fecha) + ": " + detalle + ".";
    }

    private CapacitacionResponse agendar(Long empresaId, Long operacionId, AgendarCapacitacionRequest request) {
        if (request.getFecha().isBefore(LocalDate.now(clock))) {
            throw new NegocioException("La capacitación se agenda para hoy o una fecha posterior.");
        }
        Tecnico tecnico = tecnicoService.obtener(empresaId, request.getTecnicoId());
        if (!tecnico.isActivo()) {
            throw new NegocioException("El técnico " + tecnico.getNombreCompleto() + " está inactivo.");
        }
        if (!request.isConfirmarConflicto()) {
            String aviso = avisoConflicto(empresaId, tecnico.getId(), request.getFecha());
            if (aviso != null) {
                throw new ConflictoTecnicoException(aviso);
            }
        }
        Capacitacion capacitacion = new Capacitacion(empresaRepository.getReferenceById(empresaId), operacionId, tecnico,
                request.getFecha().atTime(request.getHora()), request.getLugar().trim(), vacioANulo(request.getObservaciones()));
        return aResponse(capacitacionRepository.save(capacitacion));
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    static CapacitacionResponse aResponse(Capacitacion c) {
        return new CapacitacionResponse(c.getId(), c.getOperacionId(), c.getTecnico().getNombreCompleto(),
                c.getFechaProgramada(), c.getLugar(), c.getEstado(), c.getObservaciones());
    }
}
