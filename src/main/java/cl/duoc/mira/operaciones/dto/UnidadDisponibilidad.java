package cl.duoc.mira.operaciones.dto;

import cl.duoc.mira.inventario.entity.EstadoEquipo;
import java.time.LocalDate;

/**
 * Un equipo en la consulta de disponibilidad. Si está arrendado, trae la fecha comprometida de
 * retorno y los días de atraso, que son cero mientras el plazo no vence (HU-13).
 */
public record UnidadDisponibilidad(
        Long id,
        String numeroSerie,
        String categoria,
        String marca,
        String modelo,
        EstadoEquipo estado,
        boolean disponible,
        LocalDate fechaRetorno,
        long diasAtraso) {

    public boolean vencido() {
        return diasAtraso > 0;
    }
}
