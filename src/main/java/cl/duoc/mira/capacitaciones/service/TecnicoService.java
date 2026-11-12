package cl.duoc.mira.capacitaciones.service;

import cl.duoc.mira.capacitaciones.dto.GuardarTecnicoRequest;
import cl.duoc.mira.capacitaciones.dto.TecnicoResponse;
import cl.duoc.mira.capacitaciones.entity.Tecnico;
import cl.duoc.mira.capacitaciones.repository.TecnicoRepository;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Técnicos de la empresa (HU-30, D25), que se eligen al agendar una capacitación. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final EmpresaRepository empresaRepository;

    public List<TecnicoResponse> listar(Long empresaId) {
        return tecnicoRepository.findByEmpresaIdOrderByApellidoAscNombreAsc(empresaId).stream()
                .map(TecnicoService::aResponse).toList();
    }

    public List<TecnicoResponse> listarActivos(Long empresaId) {
        return tecnicoRepository.findByEmpresaIdAndActivoTrueOrderByApellidoAscNombreAsc(empresaId).stream()
                .map(TecnicoService::aResponse).toList();
    }

    public TecnicoResponse buscar(Long empresaId, Long id) {
        return aResponse(obtener(empresaId, id));
    }

    @Transactional
    public TecnicoResponse crear(Long empresaId, GuardarTecnicoRequest request) {
        Tecnico tecnico = new Tecnico(empresaRepository.getReferenceById(empresaId));
        aplicar(tecnico, request);
        return aResponse(tecnicoRepository.save(tecnico));
    }

    @Transactional
    public TecnicoResponse actualizar(Long empresaId, Long id, GuardarTecnicoRequest request) {
        Tecnico tecnico = obtener(empresaId, id);
        aplicar(tecnico, request);
        return aResponse(tecnico);
    }

    /** Se desactiva en lugar de eliminar: puede tener capacitaciones asociadas. */
    @Transactional
    public void cambiarEstado(Long empresaId, Long id, boolean activo) {
        obtener(empresaId, id).cambiarEstado(activo);
    }

    /** Un técnico de otra empresa se responde como inexistente (PNF-06). */
    Tecnico obtener(Long empresaId, Long id) {
        return tecnicoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El técnico"));
    }

    private static void aplicar(Tecnico tecnico, GuardarTecnicoRequest r) {
        tecnico.actualizar(r.getNombre().trim(), r.getApellido().trim(), vacioANulo(r.getEmail()), vacioANulo(r.getTelefono()));
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    static TecnicoResponse aResponse(Tecnico t) {
        return new TecnicoResponse(t.getId(), t.getNombre(), t.getApellido(), t.getEmail(), t.getTelefono(), t.isActivo());
    }
}
