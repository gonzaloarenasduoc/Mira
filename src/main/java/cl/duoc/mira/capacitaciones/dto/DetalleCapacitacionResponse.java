package cl.duoc.mira.capacitaciones.dto;

import cl.duoc.mira.capacitaciones.entity.EstadoCapacitacion;
import java.time.LocalDateTime;

/** Capacitación con su operación y su cliente, para el calendario y la ficha (HU-22). */
public record DetalleCapacitacionResponse(
        Long id,
        Long operacionId,
        String tipoOperacion,
        String cliente,
        String tecnico,
        LocalDateTime fechaProgramada,
        LocalDateTime fechaRealizada,
        String lugar,
        EstadoCapacitacion estado,
        String observaciones) {

    /** Ruta de la ficha de la operación que la origina. */
    public String rutaOperacion() {
        return ("Venta".equals(tipoOperacion) ? "/ventas/" : "/arriendos/") + operacionId;
    }

    public boolean programada() {
        return estado == EstadoCapacitacion.PROGRAMADA;
    }
}
