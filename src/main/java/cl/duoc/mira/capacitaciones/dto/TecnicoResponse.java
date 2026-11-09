package cl.duoc.mira.capacitaciones.dto;

public record TecnicoResponse(Long id, String nombre, String apellido, String email, String telefono, boolean activo) {

    public String nombreCompleto() {
        return nombre + " " + apellido;
    }
}
