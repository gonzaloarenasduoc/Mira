package cl.duoc.mira.operaciones.dto;

import java.time.LocalDate;

/** Tramo en que un equipo estuvo arrendado; {@code retornoReal} es nulo si sigue fuera (HU-27). */
public record PeriodoArrendado(Long equipoId, LocalDate inicio, LocalDate retornoReal) {
}
