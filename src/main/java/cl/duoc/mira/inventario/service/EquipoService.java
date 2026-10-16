package cl.duoc.mira.inventario.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.dto.FichaEquipoResponse;
import cl.duoc.mira.inventario.dto.FiltroEquipos;
import cl.duoc.mira.inventario.dto.MovimientoResponse;
import cl.duoc.mira.inventario.dto.RegistrarEquipoRequest;
import cl.duoc.mira.inventario.entity.Bodega;
import cl.duoc.mira.inventario.entity.Categoria;
import cl.duoc.mira.inventario.entity.Equipo;
import cl.duoc.mira.inventario.entity.Movimiento;
import cl.duoc.mira.inventario.entity.TipoMovimiento;
import cl.duoc.mira.inventario.repository.CategoriaRepository;
import cl.duoc.mira.inventario.repository.EquipoRepository;
import cl.duoc.mira.inventario.repository.MovimientoRepository;
import cl.duoc.mira.usuarios.service.UsuarioService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
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

    public List<EquipoResponse> buscar(Long empresaId, FiltroEquipos filtro) {
        String serie = filtro.getNumeroSerie() == null ? "" : filtro.getNumeroSerie().trim();
        return equipoRepository.buscar(empresaId, filtro.getCategoriaId(), filtro.getBodegaId(),
                        filtro.getEstado(), serie).stream()
                .map(EquipoService::aResponse)
                .toList();
    }

    /** Un equipo de otra empresa se responde como inexistente (PNF-06). */
    public FichaEquipoResponse ficha(Long empresaId, Long id) {
        Equipo equipo = equipoRepository.buscarFicha(empresaId, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El equipo"));
        List<Movimiento> movimientos = movimientoRepository.historial(empresaId, id);
        Set<Long> responsables = movimientos.stream().map(Movimiento::getUsuarioId).collect(Collectors.toSet());
        Map<Long, String> nombres = usuarioService.nombresPorId(empresaId, responsables);

        List<MovimientoResponse> historial = movimientos.stream()
                .map(m -> new MovimientoResponse(m.getTipo(), m.getFecha(),
                        m.getBodegaOrigen() == null ? null : m.getBodegaOrigen().getNombre(),
                        m.getBodegaDestino() == null ? null : m.getBodegaDestino().getNombre(),
                        nombres.getOrDefault(m.getUsuarioId(), "—"), m.getObservacion()))
                .toList();
        return new FichaEquipoResponse(aResponse(equipo), historial);
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static EquipoResponse aResponse(Equipo e) {
        return new EquipoResponse(e.getId(), e.getNumeroSerie(), e.getCategoria().getNombre(),
                e.getMarca(), e.getModelo(), e.getEstado(), e.getBodega().getNombre(),
                e.getFechaAdquisicion(), e.getValorAdquisicion(), e.getTarifaDiaria(),
                e.getObservaciones());
    }
}
