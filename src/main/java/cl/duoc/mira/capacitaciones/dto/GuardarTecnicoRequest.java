package cl.duoc.mira.capacitaciones.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Formulario de técnico (HU-30). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardarTecnicoRequest {

    @NotBlank(message = "Ingrese el nombre.")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres.")
    private String nombre;

    @NotBlank(message = "Ingrese el apellido.")
    @Size(max = 100, message = "El apellido admite hasta 100 caracteres.")
    private String apellido;

    @Email(message = "Ingrese un correo válido.")
    @Size(max = 150, message = "El correo admite hasta 150 caracteres.")
    private String email;

    @Size(max = 30, message = "El teléfono admite hasta 30 caracteres.")
    private String telefono;
}
