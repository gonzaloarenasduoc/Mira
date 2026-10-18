package cl.duoc.mira.usuarios.service;

import cl.duoc.mira.usuarios.entity.RolUsuario;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Usuario en sesión. Lleva la empresa para que los controladores la pasen como parámetro
 * explícito a los servicios (D07); nunca se toma de la URL ni del formulario.
 */
public record UsuarioAutenticado(
        Long id,
        Long empresaId,
        String email,
        String passwordHash,
        String nombreCompleto,
        RolUsuario rol,
        boolean activo) implements UserDetails {

    public boolean tieneRol(RolUsuario... roles) {
        return List.of(roles).contains(rol);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
