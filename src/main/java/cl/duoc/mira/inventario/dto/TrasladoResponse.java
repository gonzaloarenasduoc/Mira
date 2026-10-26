package cl.duoc.mira.inventario.dto;

import cl.duoc.mira.inventario.entity.TipoMovimiento;
import java.time.LocalDateTime;

/** Un paso de un traslado: la salida desde el origen o la llegada al destino (HU-09 y HU-10). */
public record TrasladoResponse(
        Long id,
        Long equipoId,
        String numeroSerie,
        String equipo,
        TipoMovimiento tipo,
        LocalDateTime fecha,
        String bodegaOrigen,
        String bodegaDestino,
        String responsable,
        String observacion) {
}
