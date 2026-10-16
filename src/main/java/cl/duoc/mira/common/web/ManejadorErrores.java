package cl.duoc.mira.common.web;

import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.config.MenuGlobal;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Traduce las excepciones de dominio a páginas de error. Las reglas incumplidas dentro de un
 * formulario las resuelve cada controlador, que vuelve a mostrar el formulario con el mensaje.
 */
@ControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(RecursoNoEncontradoException ex,
                               @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        // Un manejador de excepciones no recibe los atributos de MenuGlobal: se agregan aquí.
        model.addAttribute("usuarioActual", usuario);
        model.addAttribute("menu", usuario == null ? List.of() : MenuGlobal.opcionesPara(usuario.rol()));
        model.addAttribute("mensaje", ex.getMessage());
        return "error/404";
    }
}
