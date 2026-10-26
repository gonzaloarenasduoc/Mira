package cl.duoc.mira.inventario.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.common.util.Busqueda;
import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.dto.FichaEquipoResponse;
import cl.duoc.mira.inventario.dto.FiltroEquipos;
import cl.duoc.mira.inventario.dto.MovimientoResponse;
import cl.duoc.mira.inventario.dto.RegistrarEquipoRequest;
import cl.duoc.mira.inventario.entity.Bodega;
import cl.duoc.mira.inventario.entity.Categoria;
import cl.duoc.mira.inventario.entity.Equipo;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.entity.Movimiento;
import cl.duoc.mira.inventario.entity.TipoMovimiento;
import cl.duoc.mira.inventario.repository.CategoriaRepository;
import cl.duoc.mira.inventario.repository.EquipoRepository;
import cl.duoc.mira.inventario.repository.MovimientoRepository;
import cl.duoc.mira.usuarios.service.UsuarioService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro y consulta de equipos por número de serie (E002). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoRepository movimientoRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaService bodegaService;
    private final UsuarioService usuarioService;
    private final Clock clock;

    /**
     * Registra el equipo y su movimiento de ingreso en una sola transacción (HU-04): si el
     * movimiento no se puede guardar, tampoco queda el equipo.
     */
    @Transactional
    public EquipoResponse registrar(Long empresaId, Long usuarioId, RegistrarEquipoRequest request) {
        String numeroSerie = request.getNumeroSerie().trim();
        if (equipoRepository.existsByEmpresaIdAndNumeroSerieIgnoreCase(empresaId, numeroSerie)) {
            throw new NumeroSerieDuplicadoException(numeroSerie);
        }
        Categoria categoria = categoriaRepository.findByIdAndEmpresaId(request.getCategoriaId(), empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría"));
        Bodega bodega = bodegaService.obtener(empresaId, request.getBodegaId());
        if (!bodega.isActiva()) {
            throw new NegocioException("La bodega " + bodega.getNombre()
                    + " está inactiva y no admite nuevos equipos.");
        }

        Equipo equipo = new Equipo(empresaRepository.getReferenceById(empresaId), categoria, bodega,
                numeroSerie, request.getMarca().trim(), request.getModelo().trim(),
                request.getFechaAdquisicion(), request.getValorAdquisicion(),
                request.getTarifaDiaria(), vacioANulo(request.getObservaciones()));
        Movimiento ingreso = Movimiento.registrar(equipo, TipoMovimiento.INGRESO,
                LocalDateTime.now(clock), null, bodega, usuarioId, "Registro inicial del equipo");

        equipoRepository.save(equipo);
        movimientoRepository.save(ingreso);
        return aResponse(equipo);
    }

    /** El texto se busca como parámetro y con sus comodines escapados: no altera la consulta (PNF-07). */
    public List<EquipoResponse> buscar(Long empresaId, FiltroEquipos filtro) {
        return equipoRepository.buscar(empresaId, filtro.getCategoriaId(), filtro.getBodegaId(),
                        filtro.getEstado(), Busqueda.contiene(filtro.getTexto())).stream()
                .map(EquipoService::aResponse)
                .toList();
    }

    /** Datos de varios equipos por identificador, para las vistas de otros módulos. */
    public Map<Long, EquipoResponse> resumenPorId(Long empresaId, Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return equipoRepository.buscarPorIds(empresaId, ids).stream()
                .collect(Collectors.toMap(Equipo::getId, EquipoService::aResponse));
    }

    /**
     * Entrega el equipo en arriendo (HU-14): lo bloquea, exige que esté disponible y registra el
     * movimiento SALIDA_ARRIENDO ligado a la operación. Lo invoca ArriendoService dentro de su
     * propia transacción, de modo que si otra línea del arriendo falla, esto también se revierte.
     */
    @Transactional
    public void despacharEnArriendo(Long empresaId, Long usuarioId, Long equipoId, Long operacionId) {
        Equipo equipo = equipoRepository.buscarParaActualizar(empresaId, equipoId)
                .filter(Equipo::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El equipo"));
        if (equipo.getEstado() != EstadoEquipo.DISPONIBLE) {
            throw new EquipoNoDisponibleException(equipo.getNumeroSerie(), equipo.getEstado());
        }
        movimientoRepository.save(Movimiento.registrar(equipo, TipoMovimiento.SALIDA_ARRIENDO,
                LocalDateTime.now(clock), equipo.getBodega(), null, operacionId, usuarioId, null));
    }

    /**
     * Recibe el equipo de vuelta de un arriendo (HU-15): queda disponible y asignado a la bodega que lo
     * recibe, aunque sea distinta de la de salida. El movimiento RETORNO_ARRIENDO documenta el cambio
     * de bodega: no se genera un traslado adicional (03-modelo-datos.md, sección 5, regla 4).
     */
    @Transactional
    public void recibirDeArriendo(Long empresaId, Long usuarioId, Long equipoId, Long operacionId,
                                  Long bodegaId, String observacion) {
        Equipo equipo = equipoRepository.buscarParaActualizar(empresaId, equipoId)
                .filter(Equipo::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El equipo"));
        if (equipo.getEstado() != EstadoEquipo.ARRENDADO) {
            throw new NegocioException("El equipo " + equipo.getNumeroSerie() + " no está arrendado: su estado es "
                    + equipo.getEstado().getEtiqueta().toLowerCase(Locale.ROOT) + ".");
        }
        Bodega receptora = bodegaService.obtener(empresaId, bodegaId);
        if (!receptora.isActiva()) {
            throw new NegocioException("La bodega " + receptora.getNombre() + " está inactiva y no recibe equipos.");
        }
        movimientoRepository.save(Movimiento.registrar(equipo, TipoMovimiento.RETORNO_ARRIENDO,
                LocalDateTime.now(clock), null, receptora, operacionId, usuarioId, vacioANulo(observacion)));
    }

    /** Un equipo de otra empresa se responde como inexistente (PNF-06). */
    public FichaEquipoResponse ficha(Long empresaId, Long id) {
        Equipo equipo = equipoRepository.buscarFicha(empresaId, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El equipo"));
        List<Movimiento> movimientos = movimientoRepository.historial(empresaId, id);
        Set<Long> responsables = movimientos.stream().map(Movimiento::getUsuarioId).collect(Collectors.toSet());
        Map<Long, String> nombres = usuarioService.nombresPorId(empresaId, responsables);

        List<MovimientoResponse> historial = movimientos.stream()
                .map(m -> new MovimientoResponse(m.getTipo(), m.getFecha(), m.getOperacionId(),
                        m.getBodegaOrigen() == null ? null : m.getBodegaOrigen().getNombre(),
                        m.getBodegaDestino() == null ? null : m.getBodegaDestino().getNombre(),
                        nombres.getOrDefault(m.getUsuarioId(), "—"), m.getObservacion()))
                .toList();
        String trasladoHacia = equipo.getEstado() != EstadoEquipo.EN_TRANSITO ? null
                : historial.stream().filter(m -> m.tipo() == TipoMovimiento.TRASLADO_SALIDA)
                        .map(MovimientoResponse::bodegaDestino).findFirst().orElse(null);
        return new FichaEquipoResponse(aResponse(equipo), historial, trasladoHacia);
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    static EquipoResponse aResponse(Equipo e) {
        return new EquipoResponse(e.getId(), e.getNumeroSerie(), e.getCategoria().getNombre(),
                e.getMarca(), e.getModelo(), e.getEstado(), e.getBodega().getNombre(),
                e.getFechaAdquisicion(), e.getValorAdquisicion(), e.getTarifaDiaria(),
                e.getObservaciones());
    }
}
