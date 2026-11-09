package cl.duoc.mira.capacitaciones.service;

import cl.duoc.mira.capacitaciones.dto.AgendarCapacitacionRequest;
import cl.duoc.mira.capacitaciones.dto.CapacitacionResponse;
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
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Capacitaciones de las operaciones (E007). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CapacitacionService {

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

    private CapacitacionResponse agendar(Long empresaId, Long operacionId, AgendarCapacitacionRequest request) {
        if (request.getFecha().isBefore(LocalDate.now(clock))) {
            throw new NegocioException("La capacitación se agenda para hoy o una fecha posterior.");
        }
        Tecnico tecnico = tecnicoService.obtener(empresaId, request.getTecnicoId());
        if (!tecnico.isActivo()) {
            throw new NegocioException("El técnico " + tecnico.getNombreCompleto() + " está inactivo.");
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
