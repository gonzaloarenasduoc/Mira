package cl.duoc.mira.operaciones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Fila del historial de un cliente (HU-19). {@code fecha} es la fecha de la venta o el inicio del
 * arriendo. {@code estado} solo aplica a los arriendos; en una venta es nulo y su total es definitivo.
 */
public record OperacionClienteResponse(
        Long id,
        boolean venta,
        LocalDate fecha,
        List<String> equipos,
        EstadoArriendo estado,
        BigDecimal total,
        boolean totalEstimado) {
}
