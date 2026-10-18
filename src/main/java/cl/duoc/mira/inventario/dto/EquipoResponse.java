package cl.duoc.mira.inventario.dto;

import cl.duoc.mira.inventario.entity.EstadoEquipo;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EquipoResponse(
        Long id,
        String numeroSerie,
        String categoria,
        String marca,
        String modelo,
        EstadoEquipo estado,
        String bodega,
        LocalDate fechaAdquisicion,
        BigDecimal valorAdquisicion,
        BigDecimal tarifaDiaria,
        String observaciones) {
}
