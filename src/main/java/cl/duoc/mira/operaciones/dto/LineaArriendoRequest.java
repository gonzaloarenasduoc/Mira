package cl.duoc.mira.operaciones.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Un equipo ofrecido en el formulario de arriendo; solo cuentan los que se marcan para incluir. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaArriendoRequest {

    private Long equipoId;
    private boolean incluir;

    @DecimalMin(value = "0", inclusive = false, message = "La tarifa diaria debe ser mayor que cero.")
    @Digits(integer = 10, fraction = 2, message = "La tarifa diaria admite hasta 10 dígitos y 2 decimales.")
    private BigDecimal tarifaDiaria;
}
