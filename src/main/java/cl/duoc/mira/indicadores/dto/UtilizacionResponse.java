package cl.duoc.mira.indicadores.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Utilización de los equipos en un periodo (HU-27, D24): días arrendado sobre días disponibles,
 * global, por categoría y por equipo. {@code masUsados} y {@code menosUsados} no se repiten.
 */
public record UtilizacionResponse(
        LocalDate desde,
        LocalDate hasta,
        long diasPeriodo,
        Fila global,
        List<Fila> porCategoria,
        List<FilaEquipo> masUsados,
        List<FilaEquipo> menosUsados) {

    /** Un grupo de equipos: cuántos son, sus días arrendado y disponibles, y el porcentaje. */
    public record Fila(String nombre, long equipos, long diasArrendado, long diasDisponibles, double porcentaje) {
    }

    public record FilaEquipo(Long id, String numeroSerie, String equipo, String categoria,
                             long diasArrendado, long diasDisponibles, double porcentaje) {
    }
}
