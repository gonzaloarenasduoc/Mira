package cl.duoc.mira.inventario.dto;

import cl.duoc.mira.inventario.entity.TipoMovimiento;
import java.time.LocalDateTime;

public record MovimientoResponse(
        TipoMovimiento tipo,
        LocalDateTime fecha,
        String bodegaOrigen,
        String bodegaDestino,
        String responsable,
        String observacion) {
}
