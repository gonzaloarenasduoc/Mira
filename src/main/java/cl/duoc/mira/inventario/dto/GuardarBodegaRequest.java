package cl.duoc.mira.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Formulario de bodega (HU-08). Es clase y no record: th:field necesita getters y setters. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardarBodegaRequest {

    @NotBlank(message = "Ingrese el nombre de la bodega.")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres.")
    private String nombre;

    @Size(max = 200, message = "La dirección admite hasta 200 caracteres.")
    private String direccion;

    @Size(max = 100, message = "La comuna admite hasta 100 caracteres.")
    private String comuna;
}
