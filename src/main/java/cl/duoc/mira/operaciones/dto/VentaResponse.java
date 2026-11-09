package cl.duoc.mira.operaciones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Venta con sus líneas. {@code total} es la suma de los valores de sus equipos. */
public record VentaResponse(
        Long id,
        String cliente,
        String rutCliente,
        LocalDate fecha,
        String numeroOrdenCompra,
        String observaciones,
        List<LineaVentaResponse> lineas,
        BigDecimal total) {
}
