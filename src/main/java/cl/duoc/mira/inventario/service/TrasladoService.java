package cl.duoc.mira.inventario.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.util.Busqueda;
import cl.duoc.mira.inventario.dto.BodegaResponse;
import cl.duoc.mira.inventario.dto.FiltroTraslados;
import cl.duoc.mira.inventario.dto.PreparacionTrasladoResponse;
import cl.duoc.mira.inventario.dto.RegistrarTrasladoRequest;
import cl.duoc.mira.inventario.dto.TrasladoResponse;
import cl.duoc.mira.inventario.entity.Bodega;
import cl.duoc.mira.inventario.entity.Equipo;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.entity.Movimiento;
import cl.duoc.mira.inventario.entity.TipoMovimiento;
import cl.duoc.mira.inventario.repository.EquipoRepository;
import cl.duoc.mira.inventario.repository.MovimientoRepository;
import cl.duoc.mira.usuarios.service.UsuarioService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
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
 * Traslados entre bodegas (E003). Un traslado tiene dos pasos, cada uno con su movimiento: la
 * salida deja el equipo en tránsito y la llegada lo deja disponible en la bodega de destino.
 * Mientras está en tránsito, el equipo sigue asignado a la bodega de origen.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrasladoService {

    static final int TAMANO_PAGINA = 20;
    private static final Set<TipoMovimiento> TIPOS_TRASLADO =
            EnumSet.of(TipoMovimiento.TRASLADO_SALIDA, TipoMovimiento.TRASLADO_LLEGADA);
    private static final LocalDateTime SIN_DESDE = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime SIN_HASTA = LocalDateTime.of(9999, 12, 31, 0, 0);

    private final EquipoRepository equipoRepository;
    private final MovimientoRepository movimientoRepository;
    private final BodegaService bodegaService;
    private final UsuarioService usuarioService;
    private final Clock clock;

    /** Equipo y bodegas de destino posibles. Falla de inmediato si el equipo no se puede trasladar. */
    public PreparacionTrasladoResponse preparar(Long empresaId, Long equipoId) {
        Equipo equipo = equipoRepository.buscarFicha(empresaId, equipoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El equipo"));
        validarDisponible(equipo);
        List<BodegaResponse> destinos = bodegaService.listarActivas(empresaId).stream()
                .filter(b -> !b.id().equals(equipo.getBodega().getId()))
                .toList();
        return new PreparacionTrasladoResponse(EquipoService.aResponse(equipo), destinos);
    }

    /** Registra la salida (HU-09): el equipo queda en tránsito hacia la bodega de destino. */
    @Transactional
    public TrasladoResponse registrarSalida(Long empresaId, Long usuarioId, RegistrarTrasladoRequest request) {
        Equipo equipo = bloquear(empresaId, request.getEquipoId());
        validarDisponible(equipo);
        Bodega origen = equipo.getBodega();
        Bodega destino = bodegaService.obtener(empresaId, request.getBodegaDestinoId());
        if (destino.getId().equals(origen.getId())) {
            throw new NegocioException("La bodega de destino debe ser distinta de la bodega de origen ("
                    + origen.getNombre() + ").");
        }
        if (!destino.isActiva()) {
            throw new NegocioException("La bodega " + destino.getNombre() + " está inactiva y no recibe traslados.");
        }

        Movimiento salida = Movimiento.registrar(equipo, TipoMovimiento.TRASLADO_SALIDA,
                LocalDateTime.now(clock), origen, destino, usuarioId, vacioANulo(request.getObservacion()));
        movimientoRepository.save(salida);
        return aResponse(salida, nombre(empresaId, usuarioId));
    }

    /**
     * Confirma la llegada (HU-09): el equipo queda disponible y asignado a la bodega de destino
     * de su salida pendiente. El movimiento de llegada repite el origen y el destino de la salida.
     */
    @Transactional
    public TrasladoResponse confirmarLlegada(Long empresaId, Long usuarioId, Long equipoId) {
        Equipo equipo = bloquear(empresaId, equipoId);
        Movimiento salida = equipo.getEstado() != EstadoEquipo.EN_TRANSITO ? null
                : movimientoRepository.findFirstByEmpresaIdAndEquipoIdAndTipoOrderByIdDesc(
                        empresaId, equipoId, TipoMovimiento.TRASLADO_SALIDA).orElse(null);
        if (salida == null) {
            throw new NegocioException("El equipo " + equipo.getNumeroSerie()
                    + " no tiene un traslado pendiente de llegada.");
        }

        Movimiento llegada = Movimiento.registrar(equipo, TipoMovimiento.TRASLADO_LLEGADA,
                LocalDateTime.now(clock), salida.getBodegaOrigen(), salida.getBodegaDestino(), usuarioId, null);
        movimientoRepository.save(llegada);
        return aResponse(llegada, nombre(empresaId, usuarioId));
    }

    /** Traslados iniciados cuya llegada todavía no se confirma, del más antiguo al más reciente. */
    public List<TrasladoResponse> enTransito(Long empresaId) {
        List<Movimiento> salidas = movimientoRepository.trasladosEnTransito(empresaId);
        Map<Long, String> nombres = nombres(empresaId, salidas);
        return salidas.stream()
                .map(m -> aResponse(m, nombres.getOrDefault(m.getUsuarioId(), "—")))
                .toList();
    }

    /** Historial de traslados (HU-10). Las fechas son inclusivas y la página parte en cero. */
    public Page<TrasladoResponse> historial(Long empresaId, FiltroTraslados filtro, int pagina) {
        LocalDate desde = filtro.getDesde();
        LocalDate hasta = filtro.getHasta();
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new NegocioException("La fecha de inicio no puede ser posterior a la fecha de término.");
        }
        Page<Movimiento> movimientos = movimientoRepository.traslados(empresaId, TIPOS_TRASLADO,
                Busqueda.contiene(filtro.getNumeroSerie()), filtro.getBodegaId(),
                desde == null ? SIN_DESDE : desde.atStartOfDay(),
                hasta == null ? SIN_HASTA : hasta.plusDays(1).atStartOfDay(),
                PageRequest.of(Math.max(pagina, 0), TAMANO_PAGINA));
        Map<Long, String> nombres = nombres(empresaId, movimientos.getContent());
        return movimientos.map(m -> aResponse(m, nombres.getOrDefault(m.getUsuarioId(), "—")));
    }

    private Equipo bloquear(Long empresaId, Long equipoId) {
        return equipoRepository.buscarParaActualizar(empresaId, equipoId)
                .filter(Equipo::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El equipo"));
    }

    private static void validarDisponible(Equipo equipo) {
        if (equipo.getEstado() != EstadoEquipo.DISPONIBLE) {
            throw new NegocioException("El equipo " + equipo.getNumeroSerie()
                    + " no se puede trasladar porque está "
                    + equipo.getEstado().getEtiqueta().toLowerCase(Locale.ROOT)
                    + ". Solo se trasladan equipos disponibles.");
        }
    }

    private String nombre(Long empresaId, Long usuarioId) {
        return usuarioService.nombresPorId(empresaId, Set.of(usuarioId)).getOrDefault(usuarioId, "—");
    }

    private Map<Long, String> nombres(Long empresaId, List<Movimiento> movimientos) {
        Set<Long> ids = movimientos.stream().map(Movimiento::getUsuarioId).collect(Collectors.toSet());
        return ids.isEmpty() ? Map.of() : usuarioService.nombresPorId(empresaId, ids);
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static TrasladoResponse aResponse(Movimiento m, String responsable) {
        Equipo e = m.getEquipo();
        return new TrasladoResponse(m.getId(), e.getId(), e.getNumeroSerie(),
                e.getCategoria().getNombre() + " " + e.getMarca() + " " + e.getModelo(),
                m.getTipo(), m.getFecha(), m.getBodegaOrigen().getNombre(),
                m.getBodegaDestino().getNombre(), responsable, m.getObservacion());
    }
}
