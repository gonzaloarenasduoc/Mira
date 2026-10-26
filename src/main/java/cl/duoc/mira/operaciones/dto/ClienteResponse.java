package cl.duoc.mira.operaciones.dto;

public record ClienteResponse(
        Long id,
        String rut,
        String razonSocial,
        String giro,
        String direccion,
        String comuna,
        String contactoNombre,
        String contactoEmail,
        String contactoTelefono,
        boolean activo) {
}
