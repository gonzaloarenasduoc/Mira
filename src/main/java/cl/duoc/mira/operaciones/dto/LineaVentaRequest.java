package cl.duoc.mira.operaciones.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Un equipo ofrecido en el formulario de venta; solo cuentan los que se marcan para incluir. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaVentaRequest {

    private Long equipoId;
    private boolean incluir;

    @DecimalMin(value = "0", inclusive = false, message = "El valor de venta debe ser mayor que cero.")
    @Digits(integer = 10, fraction = 2, message = "El valor de venta admite hasta 10 dígitos y 2 decimales.")
    private BigDecimal valor;
}
