package cl.duoc.mira.operaciones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Arriendo con sus líneas. {@code tarifaDiariaTotal} es la suma de las tarifas de sus equipos. */
public record ArriendoResponse(
        Long id,
        String cliente,
        String rutCliente,
        LocalDate fecha,
        LocalDate fechaInicio,
        LocalDate fechaRetornoComprometida,
        String numeroOrdenCompra,
        String observaciones,
        List<LineaArriendoResponse> lineas,
        BigDecimal tarifaDiariaTotal) {
}
