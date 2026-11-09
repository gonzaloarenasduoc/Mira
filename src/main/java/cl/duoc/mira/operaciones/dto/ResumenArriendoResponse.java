package cl.duoc.mira.operaciones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Fila del listado de arriendos (HU-17). {@code total} es la suma de tarifa diaria por días de cada
 * línea (D18); es estimado mientras algún equipo no retorna.
 */
public record ResumenArriendoResponse(
        Long id,
        String cliente,
        LocalDate fechaInicio,
        LocalDate fechaRetornoComprometida,
        List<String> equipos,
        EstadoArriendo estado,
        BigDecimal total,
        boolean totalEstimado) {
}
