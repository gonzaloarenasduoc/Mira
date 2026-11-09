package cl.duoc.mira.capacitaciones.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** Fecha y hora en que se dictó la capacitación (HU-22). */
@Data
public class MarcarRealizadaRequest {

    @NotNull(message = "Indique la fecha.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Indique la hora.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;
}
