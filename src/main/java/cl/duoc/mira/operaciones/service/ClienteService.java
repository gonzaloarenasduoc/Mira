package cl.duoc.mira.operaciones.service;

import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.operaciones.dto.ClienteResponse;
import cl.duoc.mira.operaciones.entity.Cliente;
import cl.duoc.mira.operaciones.repository.ClienteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Clientes de la empresa, que se eligen al registrar una operación. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<ClienteResponse> listarActivos(Long empresaId) {
        return clienteRepository.findByEmpresaIdAndActivoTrueOrderByRazonSocial(empresaId).stream()
                .map(c -> new ClienteResponse(c.getId(), c.getRut(), c.getRazonSocial()))
                .toList();
    }

    /** Un cliente de otra empresa se responde como inexistente (PNF-06). */
    Cliente obtener(Long empresaId, Long id) {
        return clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El cliente"));
    }
}
