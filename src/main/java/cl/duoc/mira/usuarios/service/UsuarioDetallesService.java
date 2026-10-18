package cl.duoc.mira.usuarios.service;

import cl.duoc.mira.usuarios.entity.Usuario;
import cl.duoc.mira.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Conecta el inicio de sesión de Spring Security con la tabla usuario (HU-01). */
@Service
@RequiredArgsConstructor
public class UsuarioDetallesService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
        return new UsuarioAutenticado(u.getId(), u.getEmpresa().getId(), u.getEmail(),
                u.getPasswordHash(), u.getNombreCompleto(), u.getRol(), u.isActivo());
    }
}
