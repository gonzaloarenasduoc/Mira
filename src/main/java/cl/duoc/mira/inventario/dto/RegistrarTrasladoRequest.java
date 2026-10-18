package cl.duoc.mira.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Formulario de traslado (HU-09). El origen es la bodega actual del equipo, y la fecha y el
 * responsable los registra el sistema al confirmar.
 */
@Data
public class RegistrarTrasladoRequest {

    @NotNull(message = "Indique el equipo que se traslada.")
    private Long equipoId;

    @NotNull(message = "Seleccione la bodega de destino.")
    private Long bodegaDestinoId;

    @Size(max = 300, message = "La observación admite hasta 300 caracteres.")
    private String observacion;
}
