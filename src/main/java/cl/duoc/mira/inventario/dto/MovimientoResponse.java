package cl.duoc.mira.inventario.dto;

import cl.duoc.mira.inventario.entity.TipoMovimiento;
import java.time.LocalDateTime;

/** Un movimiento del historial; {@code operacionId} enlaza la venta o el arriendo que lo originó. */
public record MovimientoResponse(
        TipoMovimiento tipo,
        LocalDateTime fecha,
        Long operacionId,
        String bodegaOrigen,
        String bodegaDestino,
        String responsable,
        String observacion) {
}
