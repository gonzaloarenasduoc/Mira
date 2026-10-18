package cl.duoc.mira.usuarios.dto;

import cl.duoc.mira.usuarios.entity.RolUsuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        RolUsuario rol,
        boolean activo) {

    public String nombreCompleto() {
        return nombre + " " + apellido;
    }
}
