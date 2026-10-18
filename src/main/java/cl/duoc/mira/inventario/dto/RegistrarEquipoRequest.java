package cl.duoc.mira.inventario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** Formulario de registro de equipo (HU-04). Es clase y no record: th:field necesita getters y setters. */
@Data
public class RegistrarEquipoRequest {

    @NotNull(message = "Seleccione la categoría.")
    private Long categoriaId;

    @NotNull(message = "Seleccione la bodega.")
    private Long bodegaId;

    @NotBlank(message = "Ingrese el número de serie.")
    @Size(max = 80, message = "El número de serie admite hasta 80 caracteres.")
    private String numeroSerie;

    @NotBlank(message = "Ingrese la marca.")
    @Size(max = 80, message = "La marca admite hasta 80 caracteres.")
    private String marca;

    @NotBlank(message = "Ingrese el modelo.")
    @Size(max = 80, message = "El modelo admite hasta 80 caracteres.")
    private String modelo;

    @PastOrPresent(message = "La fecha de adquisición no puede ser futura.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaAdquisicion;

    @DecimalMin(value = "0", message = "El valor de adquisición no puede ser negativo.")
    @Digits(integer = 10, fraction = 2, message = "El valor de adquisición admite hasta 10 dígitos y 2 decimales.")
    private BigDecimal valorAdquisicion;

    @DecimalMin(value = "0", message = "La tarifa diaria no puede ser negativa.")
    @Digits(integer = 10, fraction = 2, message = "La tarifa diaria admite hasta 10 dígitos y 2 decimales.")
    private BigDecimal tarifaDiaria;

    @Size(max = 2000, message = "Las observaciones admiten hasta 2000 caracteres.")
    private String observaciones;
}
