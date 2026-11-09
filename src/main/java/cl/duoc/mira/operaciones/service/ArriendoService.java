package cl.duoc.mira.operaciones.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.dto.FiltroEquipos;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.service.EquipoNoDisponibleException;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.AlertaRetornoResponse;
import cl.duoc.mira.operaciones.dto.ArriendoResponse;
import cl.duoc.mira.operaciones.dto.EstadoArriendo;
import cl.duoc.mira.operaciones.dto.LineaArriendoRequest;
import cl.duoc.mira.operaciones.dto.LineaArriendoResponse;
import cl.duoc.mira.operaciones.dto.RegistrarArriendoRequest;
import cl.duoc.mira.operaciones.dto.RegistrarRetornoRequest;
import cl.duoc.mira.operaciones.dto.ResumenArriendoResponse;
import cl.duoc.mira.operaciones.entity.Arriendo;
import cl.duoc.mira.operaciones.entity.Cliente;
import cl.duoc.mira.operaciones.entity.OperacionDetalle;
import cl.duoc.mira.operaciones.repository.ArriendoRepository;
import cl.duoc.mira.operaciones.repository.ArriendoVigente;
import cl.duoc.mira.operaciones.repository.LineaPorRetornar;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro de arriendos (E005). Registrar un arriendo crea la operación y sus líneas, cambia el
 * estado de cada equipo y registra su movimiento: todo en una sola transacción (03-modelo-datos.md,
 * sección 5, reglas 1 y 2). Los equipos se modifican solo a través de EquipoService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArriendoService {

    /** Días de anticipación con que se avisa un retorno próximo (HU-16). */
    static final int DIAS_DE_AVISO = 3;

    /** Arriendos por página en el listado (HU-17). */
    static final int TAMANO_PAGINA = 20;

    private static final DateTimeFormatter FECHA_LARGA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CL"));

    private final ArriendoRepository arriendoRepository;
    private final ClienteService clienteService;
    private final EquipoService equipoService;
    private final EmpresaRepository empresaRepository;
    private final Clock clock;

    /**
     * Registra el arriendo (HU-14). Si cualquier línea falla, la excepción revierte la transacción
     * completa: no queda el arriendo, ni equipos arrendados, ni movimientos (CF-03).
     */
    @Transactional
    public ArriendoResponse registrar(Long empresaId, Long usuarioId, RegistrarArriendoRequest request) {
        List<LineaArriendoRequest> lineas = request.getLineas().stream().filter(LineaArriendoRequest::isIncluir).toList();
        validar(request, lineas);
        Cliente cliente = clienteService.obtener(empresaId, request.getClienteId());
        if (!cliente.isActivo()) {
            throw new NegocioException("El cliente " + cliente.getRazonSocial() + " está inactivo.");
        }
        Set<Long> ids = lineas.stream().map(LineaArriendoRequest::getEquipoId).collect(Collectors.toSet());
        if (equipoService.resumenPorId(empresaId, ids).size() != ids.size()) {
            throw new RecursoNoEncontradoException("El equipo");
        }
        lineas.forEach(l -> rechazarSiTieneArriendoVigente(empresaId, l.getEquipoId()));

        Arriendo arriendo = new Arriendo(empresaRepository.getReferenceById(empresaId), cliente, usuarioId,
                LocalDate.now(clock), request.getFechaInicio(), request.getFechaRetornoComprometida(),
                vacioANulo(request.getNumeroOrdenCompra()), vacioANulo(request.getObservaciones()));
        lineas.forEach(l -> arriendo.agregarLinea(l.getEquipoId(), l.getTarifaDiaria()));
        arriendoRepository.save(arriendo);

        for (LineaArriendoRequest linea : lineas) {
            try {
                equipoService.despacharEnArriendo(empresaId, usuarioId, linea.getEquipoId(), arriendo.getId());
            } catch (EquipoNoDisponibleException ex) {
                throw new NegocioException(conFechaDeRetorno(empresaId, linea.getEquipoId(), ex));
            }
        }
        return aResponse(empresaId, arriendo);
    }

    /**
     * Registra el retorno de un equipo del arriendo (HU-15, CF-04): escribe la fecha de retorno real
     * en su línea y el equipo vuelve a quedar disponible en la bodega que lo recibe, todo en una sola
     * transacción. Los demás equipos del arriendo siguen arrendados.
     */
    @Transactional
    public ArriendoResponse registrarRetorno(Long empresaId, Long usuarioId, Long arriendoId,
                                             RegistrarRetornoRequest request) {
        Arriendo arriendo = arriendoRepository.buscarFicha(empresaId, arriendoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El arriendo"));
        OperacionDetalle linea = arriendo.getDetalles().stream()
                .filter(d -> d.getEquipoId().equals(request.getEquipoId()))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("El equipo en este arriendo"));
        String serie = equipoService.resumenPorId(empresaId, Set.of(linea.getEquipoId())).values().stream()
                .map(EquipoResponse::numeroSerie).findFirst().orElse("seleccionado");
        if (!linea.estaPendienteDeRetorno()) {
            throw new NegocioException("El equipo " + serie + " ya retornó el "
                    + FECHA_LARGA.format(linea.getFechaRetornoReal()) + ".");
        }
        LocalDate fecha = request.getFechaRetorno();
        if (fecha.isBefore(arriendo.getFechaInicio())) {
            throw new NegocioException("La fecha de retorno no puede ser anterior al inicio del arriendo ("
                    + FECHA_LARGA.format(arriendo.getFechaInicio()) + ").");
        }
        if (fecha.isAfter(LocalDate.now(clock))) {
            throw new NegocioException("La fecha de retorno no puede ser futura.");
        }

        String observacion = vacioANulo(request.getObservacion());
        arriendo.registrarRetorno(linea, fecha, observacion);
        equipoService.recibirDeArriendo(empresaId, usuarioId, linea.getEquipoId(), arriendo.getId(),
                request.getBodegaId(), observacion);
        return aResponse(empresaId, arriendo);
    }

    /**
     * Alertas de retorno (HU-16, CF-10): equipos sin retornar cuya fecha comprometida vence en
     * {@value #DIAS_DE_AVISO} días o menos, o que ya venció. Primero los más atrasados.
     */
    public List<AlertaRetornoResponse> alertasDeRetorno(Long empresaId) {
        LocalDate hoy = LocalDate.now(clock);
        List<LineaPorRetornar> lineas = arriendoRepository.porRetornarHasta(empresaId, hoy.plusDays(DIAS_DE_AVISO));
        Map<Long, EquipoResponse> equipos = equipoService.resumenPorId(empresaId,
                lineas.stream().map(LineaPorRetornar::equipoId).collect(Collectors.toSet()));
        return lineas.stream()
                .map(l -> {
                    EquipoResponse e = equipos.get(l.equipoId());
                    return new AlertaRetornoResponse(l.arriendoId(), l.cliente(), l.equipoId(),
                            e == null ? "—" : e.numeroSerie(),
                            e == null ? "—" : e.categoria() + " " + e.marca() + " " + e.modelo(),
                            l.fechaRetornoComprometida(),
                            ChronoUnit.DAYS.between(hoy, l.fechaRetornoComprometida()));
                })
                .toList();
    }

    /** Equipos que se pueden arrendar: estado disponible y sin arriendo vigente (D05). */
    public List<EquipoResponse> equiposDisponibles(Long empresaId) {
        FiltroEquipos filtro = new FiltroEquipos();
        filtro.setEstado(EstadoEquipo.DISPONIBLE);
        Set<Long> conArriendo = arriendoRepository.vigentes(empresaId).stream()
                .map(ArriendoVigente::equipoId).collect(Collectors.toSet());
        return equipoService.buscar(empresaId, filtro).stream()
                .filter(e -> !conArriendo.contains(e.id()))
                .toList();
    }

    /** Si el arriendo existe y es de la empresa; lo usan otros módulos, como documentos (HU-23). */
    public boolean existe(Long empresaId, Long id) {
        return arriendoRepository.existsByIdAndEmpresaId(id, empresaId);
    }

    /** Un arriendo de otra empresa se responde como inexistente (PNF-06). */
    public ArriendoResponse ficha(Long empresaId, Long id) {
        return aResponse(empresaId, arriendoRepository.buscarFicha(empresaId, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El arriendo")));
    }

    private static void validar(RegistrarArriendoRequest request, List<LineaArriendoRequest> lineas) {
        if (lineas.isEmpty()) {
            throw new NegocioException("Seleccione al menos un equipo para arrendar.");
        }
        if (!request.getFechaRetornoComprometida().isAfter(request.getFechaInicio())) {
            throw new NegocioException("La fecha de retorno comprometida debe ser posterior a la fecha de inicio.");
        }
        Set<Long> vistos = new HashSet<>();
        for (LineaArriendoRequest linea : lineas) {
            if (linea.getEquipoId() == null || !vistos.add(linea.getEquipoId())) {
                throw new NegocioException("Cada equipo puede incluirse una sola vez en el arriendo.");
            }
            if (linea.getTarifaDiaria() == null || linea.getTarifaDiaria().signum() <= 0) {
                throw new NegocioException("Indique una tarifa diaria mayor que cero para cada equipo seleccionado.");
            }
        }
    }

    /** Regla 1: un equipo con un arriendo sin retorno no se vuelve a arrendar, aunque su estado lo permita. */
    private void rechazarSiTieneArriendoVigente(Long empresaId, Long equipoId) {
        arriendoRepository.vigentesDelEquipo(empresaId, equipoId).stream()
                .map(ArriendoVigente::fechaRetornoComprometida)
                .min(LocalDate::compareTo)
                .ifPresent(fecha -> {
                    String serie = equipoService.resumenPorId(empresaId, Set.of(equipoId)).values().stream()
                            .map(EquipoResponse::numeroSerie).findFirst().orElse("seleccionado");
                    throw new NegocioException("El equipo " + serie + " no está disponible porque se encuentra arrendado hasta el "
                            + FECHA_LARGA.format(fecha) + ".");
                });
    }

    /**
     * Listado de arriendos con su estado y su total (HU-17), del inicio más reciente al más antiguo.
     * Tres consultas por página: los arriendos con su cliente, sus líneas y los equipos de esas líneas.
     */
    public Page<ResumenArriendoResponse> listar(Long empresaId, int pagina) {
        Page<Arriendo> arriendos = arriendoRepository.listar(empresaId, PageRequest.of(Math.max(pagina, 0), TAMANO_PAGINA));
        if (arriendos.isEmpty()) {
            return Page.empty(arriendos.getPageable());
        }
        arriendoRepository.conDetalles(arriendos.map(Arriendo::getId).toList());
        Set<Long> ids = arriendos.stream().flatMap(a -> a.getDetalles().stream())
                .map(OperacionDetalle::getEquipoId).collect(Collectors.toSet());
        Map<Long, EquipoResponse> equipos = equipoService.resumenPorId(empresaId, ids);
        LocalDate hoy = LocalDate.now(clock);
        return arriendos.map(a -> new ResumenArriendoResponse(a.getId(), a.getCliente().getRazonSocial(),
                a.getFechaInicio(), a.getFechaRetornoComprometida(),
                a.getDetalles().stream().map(d -> equipos.containsKey(d.getEquipoId())
                        ? equipos.get(d.getEquipoId()).numeroSerie() : "—").toList(),
                estado(a, hoy), total(a, hoy), a.getDetalles().stream().anyMatch(OperacionDetalle::estaPendienteDeRetorno)));
    }

    /** Cerrado si todos sus equipos retornaron; vencido si alguno sigue fuera después de la fecha comprometida. */
    static EstadoArriendo estado(Arriendo a, LocalDate hoy) {
        if (a.getDetalles().stream().noneMatch(OperacionDetalle::estaPendienteDeRetorno)) {
            return EstadoArriendo.CERRADO;
        }
        return hoy.isAfter(a.getFechaRetornoComprometida()) ? EstadoArriendo.VENCIDO : EstadoArriendo.VIGENTE;
    }

    /**
     * Suma de tarifa diaria por días de cada línea (D18). Los días son la diferencia entre el inicio y el
     * fin, con mínimo uno. El fin es la fecha de retorno real; si el equipo no ha vuelto, la fecha
     * comprometida, o hoy si ya venció, para que el total estimado refleje el atraso.
     */
    static BigDecimal total(Arriendo a, LocalDate hoy) {
        return a.getDetalles().stream()
                .map(d -> d.getValorUnitario().multiply(BigDecimal.valueOf(dias(a, d, hoy))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static long dias(Arriendo a, OperacionDetalle d, LocalDate hoy) {
        LocalDate fin = d.getFechaRetornoReal() != null ? d.getFechaRetornoReal()
                : hoy.isAfter(a.getFechaRetornoComprometida()) ? hoy : a.getFechaRetornoComprometida();
        return Math.max(1, ChronoUnit.DAYS.between(a.getFechaInicio(), fin));
    }

    /** PNF-10: "El equipo GPS-123 no está disponible porque se encuentra arrendado hasta el 25 de marzo". */
    private String conFechaDeRetorno(Long empresaId, Long equipoId, EquipoNoDisponibleException ex) {
        if (ex.getEstado() != EstadoEquipo.ARRENDADO) {
            return ex.getMessage();
        }
        return arriendoRepository.vigentesDelEquipo(empresaId, equipoId).stream()
                .map(ArriendoVigente::fechaRetornoComprometida)
                .min(LocalDate::compareTo)
                .map(f -> "El equipo " + ex.getNumeroSerie() + " no está disponible porque se encuentra arrendado hasta el "
                        + FECHA_LARGA.format(f) + ".")
                .orElse(ex.getMessage());
    }

    private ArriendoResponse aResponse(Long empresaId, Arriendo a) {
        Set<Long> ids = a.getDetalles().stream().map(OperacionDetalle::getEquipoId).collect(Collectors.toSet());
        Map<Long, EquipoResponse> equipos = equipoService.resumenPorId(empresaId, ids);
        List<LineaArriendoResponse> lineas = a.getDetalles().stream()
                .map(d -> {
                    EquipoResponse e = equipos.get(d.getEquipoId());
                    return new LineaArriendoResponse(d.getEquipoId(),
                            e == null ? "—" : e.numeroSerie(),
                            e == null ? "—" : e.categoria() + " " + e.marca() + " " + e.modelo(),
                            d.getValorUnitario(), d.getFechaRetornoReal());
                })
                .toList();
        BigDecimal total = lineas.stream().map(LineaArriendoResponse::tarifaDiaria).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ArriendoResponse(a.getId(), a.getCliente().getRazonSocial(), a.getCliente().getRut(),
                a.getFecha(), a.getFechaInicio(), a.getFechaRetornoComprometida(), a.getNumeroOrdenCompra(),
                a.getObservaciones(), lineas, total);
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
