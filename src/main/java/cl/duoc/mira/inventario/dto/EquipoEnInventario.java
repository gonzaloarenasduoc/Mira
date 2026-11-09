package cl.duoc.mira.inventario.dto;

import java.time.LocalDate;

/** Equipo del inventario (no vendido) con su fecha de ingreso, para la utilización (HU-27). */
public record EquipoEnInventario(Long id, String numeroSerie, String categoria, String equipo, LocalDate ingreso) {
}
