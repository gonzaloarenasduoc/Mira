package cl.duoc.mira.operaciones.dto;

import java.time.LocalDate;
import java.util.List;

/** Disponibilidad consolidada de todas las bodegas, calculada a la fecha {@code hoy}. */
public record DisponibilidadResponse(List<BodegaDisponibilidad> bodegas, long disponibles, long unidades,
                                     LocalDate hoy) {
}
