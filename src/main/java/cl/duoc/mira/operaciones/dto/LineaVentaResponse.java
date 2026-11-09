package cl.duoc.mira.operaciones.dto;

import java.math.BigDecimal;

public record LineaVentaResponse(
        Long equipoId,
        String numeroSerie,
        String equipo,
        BigDecimal valor) {
}
