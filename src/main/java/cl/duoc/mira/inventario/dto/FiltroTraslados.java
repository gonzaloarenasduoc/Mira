package cl.duoc.mira.inventario.dto;

import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** Filtros del historial de traslados (HU-10). Todos son opcionales. */
@Data
public class FiltroTraslados {

    private String numeroSerie;
    private Long bodegaId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;
}
