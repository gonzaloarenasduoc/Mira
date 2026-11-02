package cl.duoc.mira.operaciones.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.common.util.Rut;
import cl.duoc.mira.operaciones.dto.ClienteResponse;
import cl.duoc.mira.operaciones.dto.GuardarClienteRequest;
import cl.duoc.mira.operaciones.entity.Cliente;
import cl.duoc.mira.operaciones.repository.ClienteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Clientes de la empresa (HU-29, D17), que se eligen al registrar una operación. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    public List<ClienteResponse> listar(Long empresaId) {
        return clienteRepository.findByEmpresaIdOrderByRazonSocial(empresaId).stream()
                .map(ClienteService::aResponse).toList();
    }

    public List<ClienteResponse> listarActivos(Long empresaId) {
        return clienteRepository.findByEmpresaIdAndActivoTrueOrderByRazonSocial(empresaId).stream()
                .map(ClienteService::aResponse).toList();
    }

    public ClienteResponse buscar(Long empresaId, Long id) {
        return aResponse(obtener(empresaId, id));
    }

    /** El RUT se valida con su dígito verificador y es único dentro de la empresa. */
    @Transactional
    public ClienteResponse crear(Long empresaId, GuardarClienteRequest request) {
        String rut = Rut.normalizar(request.getRut())
                .orElseThrow(() -> new NegocioException("El RUT ingresado no es válido. Revise el número y el dígito verificador."));
        clienteRepository.findByEmpresaIdAndRut(empresaId, rut).ifPresent(otro -> {
            String nombre = otro.getRazonSocial();
            throw new NegocioException("El RUT " + rut + " ya está registrado para " + nombre
                    + (nombre.endsWith(".") ? "" : "."));
        });
        Cliente cliente = new Cliente(empresaRepository.getReferenceById(empresaId), rut);
        aplicar(cliente, request);
        return aResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long empresaId, Long id, GuardarClienteRequest request) {
        Cliente cliente = obtener(empresaId, id);
        aplicar(cliente, request);
        return aResponse(cliente);
    }

    /** Se desactiva en lugar de eliminar: el cliente tiene operaciones asociadas. */
    @Transactional
    public void cambiarEstado(Long empresaId, Long id, boolean activo) {
        obtener(empresaId, id).cambiarEstado(activo);
    }

    /** Un cliente de otra empresa se responde como inexistente (PNF-06). */
    Cliente obtener(Long empresaId, Long id) {
        return clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El cliente"));
    }

    private static void aplicar(Cliente cliente, GuardarClienteRequest r) {
        cliente.actualizar(r.getRazonSocial().trim(), vacioANulo(r.getGiro()), vacioANulo(r.getDireccion()),
                vacioANulo(r.getComuna()), vacioANulo(r.getContactoNombre()), vacioANulo(r.getContactoEmail()),
                vacioANulo(r.getContactoTelefono()));
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static ClienteResponse aResponse(Cliente c) {
        return new ClienteResponse(c.getId(), c.getRut(), c.getRazonSocial(), c.getGiro(), c.getDireccion(),
                c.getComuna(), c.getContactoNombre(), c.getContactoEmail(), c.getContactoTelefono(), c.isActivo());
    }
}
