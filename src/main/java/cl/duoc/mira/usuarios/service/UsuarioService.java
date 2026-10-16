package cl.duoc.mira.usuarios.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.usuarios.dto.CrearUsuarioRequest;
import cl.duoc.mira.usuarios.dto.EditarUsuarioRequest;
import cl.duoc.mira.usuarios.dto.UsuarioResponse;
import cl.duoc.mira.usuarios.entity.Usuario;
import cl.duoc.mira.usuarios.repository.UsuarioRepository;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestión de usuarios (E001). Toda operación recibe la empresa del usuario en sesión (D07). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listar(Long empresaId) {
        return usuarioRepository.findByEmpresaIdOrderByApellidoAscNombreAsc(empresaId).stream()
                .map(UsuarioService::aResponse)
                .toList();
    }

    public UsuarioResponse buscar(Long empresaId, Long id) {
        return aResponse(obtener(empresaId, id));
    }

    @Transactional
    public UsuarioResponse crear(Long empresaId, CrearUsuarioRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new CorreoDuplicadoException(email);
        }
        Usuario usuario = new Usuario(empresaRepository.getReferenceById(empresaId),
                request.getNombre().trim(), request.getApellido().trim(), email,
                passwordEncoder.encode(request.getPassword()), request.getRol());
        return aResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long empresaId, Long id, EditarUsuarioRequest request) {
        Usuario usuario = obtener(empresaId, id);
        usuario.actualizar(request.getNombre().trim(), request.getApellido().trim(), request.getRol());
        return aResponse(usuario);
    }

    /** Un usuario inactivo no puede iniciar sesión; se conserva para no perder su historial. */
    @Transactional
    public void cambiarEstado(Long empresaId, Long id, boolean activo, Long idSolicitante) {
        if (!activo && id.equals(idSolicitante)) {
            throw new NegocioException("No puede desactivar su propio usuario.");
        }
        obtener(empresaId, id).cambiarEstado(activo);
    }

    /** Nombres de los responsables de un historial, en una sola consulta. */
    public Map<Long, String> nombresPorId(Long empresaId, Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return usuarioRepository.findByEmpresaIdAndIdIn(empresaId, ids).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNombreCompleto));
    }

    private Usuario obtener(Long empresaId, Long id) {
        return usuarioRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario"));
    }

    private static UsuarioResponse aResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellido(), u.getEmail(),
                u.getRol(), u.isActivo());
    }
}
