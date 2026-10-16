package cl.duoc.mira.inventario.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.inventario.dto.BodegaResponse;
import cl.duoc.mira.inventario.dto.GuardarBodegaRequest;
import cl.duoc.mira.inventario.entity.Bodega;
import cl.duoc.mira.inventario.repository.BodegaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Administración de las bodegas de la empresa (HU-08). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BodegaService {

    private final BodegaRepository bodegaRepository;
    private final EmpresaRepository empresaRepository;

    public List<BodegaResponse> listar(Long empresaId) {
        return bodegaRepository.findByEmpresaIdOrderByNombre(empresaId).stream()
                .map(BodegaService::aResponse).toList();
    }

    public List<BodegaResponse> listarActivas(Long empresaId) {
        return bodegaRepository.findByEmpresaIdAndActivaTrueOrderByNombre(empresaId).stream()
                .map(BodegaService::aResponse).toList();
    }

    public BodegaResponse buscar(Long empresaId, Long id) {
        return aResponse(obtener(empresaId, id));
    }

    @Transactional
    public BodegaResponse crear(Long empresaId, GuardarBodegaRequest request) {
        String nombre = request.getNombre().trim();
        validarNombreLibre(empresaId, nombre, null);
        Bodega bodega = new Bodega(empresaRepository.getReferenceById(empresaId), nombre,
                vacioANulo(request.getDireccion()), vacioANulo(request.getComuna()));
        return aResponse(bodegaRepository.save(bodega));
    }

    @Transactional
    public BodegaResponse actualizar(Long empresaId, Long id, GuardarBodegaRequest request) {
        Bodega bodega = obtener(empresaId, id);
        String nombre = request.getNombre().trim();
        validarNombreLibre(empresaId, nombre, id);
        bodega.actualizar(nombre, vacioANulo(request.getDireccion()), vacioANulo(request.getComuna()));
        return aResponse(bodega);
    }

    /** Se desactiva en lugar de eliminar: la bodega tiene equipos y movimientos asociados. */
    @Transactional
    public void cambiarEstado(Long empresaId, Long id, boolean activa) {
        obtener(empresaId, id).cambiarEstado(activa);
    }

    Bodega obtener(Long empresaId, Long id) {
        return bodegaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La bodega"));
    }

    private void validarNombreLibre(Long empresaId, String nombre, Long idPropio) {
        bodegaRepository.findByEmpresaIdAndNombreIgnoreCase(empresaId, nombre)
                .filter(otra -> !otra.getId().equals(idPropio))
                .ifPresent(otra -> {
                    throw new NegocioException("Ya existe una bodega llamada " + otra.getNombre() + ".");
                });
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static BodegaResponse aResponse(Bodega b) {
        return new BodegaResponse(b.getId(), b.getNombre(), b.getDireccion(), b.getComuna(), b.isActiva());
    }
}
