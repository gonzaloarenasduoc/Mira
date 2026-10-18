package cl.duoc.mira.usuarios.dto;

import cl.duoc.mira.usuarios.entity.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Formulario de creación de usuario (HU-02). Es clase y no record: th:field necesita getters y setters. */
@Data
public class CrearUsuarioRequest {

    @NotBlank(message = "Ingrese el nombre.")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "Ingrese el apellido.")
    @Size(max = 100)
    private String apellido;

    @NotBlank(message = "Ingrese el correo.")
    @Email(message = "El correo no tiene un formato válido.")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Ingrese una contraseña inicial.")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    private String password;

    @NotNull(message = "Seleccione un rol.")
    private RolUsuario rol;
}
