package cl.duoc.mira.operaciones.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** Formulario de retorno de un equipo arrendado (HU-15). */
@Data
public class RegistrarRetornoRequest {

    @NotNull(message = "Indique el equipo que retorna.")
    private Long equipoId;

    @NotNull(message = "Seleccione la bodega que recibe el equipo.")
    private Long bodegaId;

    @NotNull(message = "Indique la fecha de retorno.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaRetorno;

    @Size(max = 300, message = "La observación admite hasta 300 caracteres.")
    private String observacion;
}
