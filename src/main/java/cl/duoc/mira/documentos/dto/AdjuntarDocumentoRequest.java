package cl.duoc.mira.documentos.dto;

import cl.duoc.mira.documentos.entity.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** Formulario para adjuntar un documento a una operación (HU-23). El archivo llega aparte. */
@Data
public class AdjuntarDocumentoRequest {

    @NotNull(message = "Seleccione el tipo de documento.")
    private TipoDocumento tipo;

    @NotBlank(message = "Ingrese el número o folio del documento.")
    @Size(max = 50, message = "El número o folio admite hasta 50 caracteres.")
    private String numeroFolio;

    @NotNull(message = "Indique la fecha del documento.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaEmision;
}
