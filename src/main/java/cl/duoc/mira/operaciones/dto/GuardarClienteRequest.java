package cl.duoc.mira.operaciones.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Formulario de cliente (HU-29). El RUT solo se ingresa al crearlo. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardarClienteRequest {

    @Size(max = 15, message = "El RUT admite hasta 15 caracteres.")
    private String rut;

    @NotBlank(message = "Ingrese la razón social.")
    @Size(max = 200, message = "La razón social admite hasta 200 caracteres.")
    private String razonSocial;

    @Size(max = 150, message = "El giro admite hasta 150 caracteres.")
    private String giro;

    @Size(max = 200, message = "La dirección admite hasta 200 caracteres.")
    private String direccion;

    @Size(max = 100, message = "La comuna admite hasta 100 caracteres.")
    private String comuna;

    @Size(max = 150, message = "El nombre del contacto admite hasta 150 caracteres.")
    private String contactoNombre;

    @Email(message = "Ingrese un correo válido.")
    @Size(max = 150, message = "El correo del contacto admite hasta 150 caracteres.")
    private String contactoEmail;

    @Size(max = 30, message = "El teléfono admite hasta 30 caracteres.")
    private String contactoTelefono;
}
