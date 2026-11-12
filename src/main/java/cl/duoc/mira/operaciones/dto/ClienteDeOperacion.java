package cl.duoc.mira.operaciones.dto;

/** Cliente de una operación, para las vistas de otros módulos, como el calendario de capacitaciones (HU-22). */
public record ClienteDeOperacion(Long operacionId, String cliente) {
}
