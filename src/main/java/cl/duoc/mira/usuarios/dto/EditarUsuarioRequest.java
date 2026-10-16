package cl.duoc.mira.usuarios.dto;

import cl.duoc.mira.usuarios.entity.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Formulario de edición de usuario (HU-06). El correo no se edita: es la credencial de acceso. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditarUsuarioRequest {

    @NotBlank(message = "Ingrese el nombre.")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "Ingrese el apellido.")
    @Size(max = 100)
    private String apellido;

    @NotNull(message = "Seleccione un rol.")
    private RolUsuario rol;
}
