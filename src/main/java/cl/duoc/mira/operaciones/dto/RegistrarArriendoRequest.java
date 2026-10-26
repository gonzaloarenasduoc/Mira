package cl.duoc.mira.operaciones.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** Formulario de registro de arriendo (HU-14). Es clase y no record: th:field necesita getters y setters. */
@Data
public class RegistrarArriendoRequest {

    @NotNull(message = "Seleccione el cliente.")
    private Long clienteId;

    @NotNull(message = "Indique la fecha de inicio.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @NotNull(message = "Indique la fecha de retorno comprometida.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaRetornoComprometida;

    @Size(max = 50, message = "La orden de compra admite hasta 50 caracteres.")
    private String numeroOrdenCompra;

    @Size(max = 2000, message = "Las observaciones admiten hasta 2000 caracteres.")
    private String observaciones;

    @Valid
    private List<LineaArriendoRequest> lineas = new ArrayList<>();
}
