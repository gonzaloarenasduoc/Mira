package cl.duoc.mira.capacitaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** Formulario para agendar una capacitación (HU-20). */
@Data
public class AgendarCapacitacionRequest {

    @NotNull(message = "Seleccione el técnico.")
    private Long tecnicoId;

    @NotNull(message = "Indique la fecha.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Indique la hora.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;

    @NotBlank(message = "Indique el lugar.")
    @Size(max = 200, message = "El lugar admite hasta 200 caracteres.")
    private String lugar;

    @Size(max = 2000, message = "Las observaciones admiten hasta 2000 caracteres.")
    private String observaciones;

    /** HU-21: el vendedor vio el aviso de técnico ocupado y agenda de todas formas. */
    private boolean confirmarConflicto;
}
