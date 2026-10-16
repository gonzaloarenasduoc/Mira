package cl.duoc.mira.usuarios.controller;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.usuarios.dto.CrearUsuarioRequest;
import cl.duoc.mira.usuarios.dto.EditarUsuarioRequest;
import cl.duoc.mira.usuarios.dto.UsuarioResponse;
import cl.duoc.mira.usuarios.entity.RolUsuario;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import cl.duoc.mira.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Administración de usuarios (HU-02 y HU-06). Solo el administrador llega aquí (SeguridadConfig). */
@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @ModelAttribute("seccion")
    public String seccion() {
        return "Usuarios";
    }

    @ModelAttribute("roles")
    public RolUsuario[] roles() {
        return RolUsuario.values();
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado actual, Model model) {
        model.addAttribute("usuarios", usuarioService.listar(actual.empresaId()));
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new CrearUsuarioRequest());
        model.addAttribute("modo", "crear");
        return "usuarios/formulario";
    }

    @PostMapping
    public String crear(@AuthenticationPrincipal UsuarioAutenticado actual,
                        @Valid @ModelAttribute("usuario") CrearUsuarioRequest usuario,
                        BindingResult errores, Model model, RedirectAttributes redirect) {
        model.addAttribute("modo", "crear");
        if (errores.hasErrors()) {
            return "usuarios/formulario";
        }
        try {
            UsuarioResponse creado = usuarioService.crear(actual.empresaId(), usuario);
            redirect.addFlashAttribute("exito", "Se creó el usuario " + creado.nombreCompleto() + ".");
            return "redirect:/usuarios";
        } catch (NegocioException ex) {
            errores.reject("negocio", ex.getMessage());
            return "usuarios/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        UsuarioResponse u = usuarioService.buscar(actual.empresaId(), id);
        model.addAttribute("usuario", new EditarUsuarioRequest(u.nombre(), u.apellido(), u.rol()));
        model.addAttribute("email", u.email());
        model.addAttribute("id", id);
        model.addAttribute("modo", "editar");
        return "usuarios/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                             @Valid @ModelAttribute("usuario") EditarUsuarioRequest usuario,
                             BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            model.addAttribute("email", usuarioService.buscar(actual.empresaId(), id).email());
            model.addAttribute("id", id);
            model.addAttribute("modo", "editar");
            return "usuarios/formulario";
        }
        usuarioService.actualizar(actual.empresaId(), id, usuario);
        redirect.addFlashAttribute("exito", "Se actualizaron los datos del usuario.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                             RedirectAttributes redirect) {
        return cambiarEstado(actual, id, false, redirect);
    }

    @PostMapping("/{id}/activar")
    public String activar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                          RedirectAttributes redirect) {
        return cambiarEstado(actual, id, true, redirect);
    }

    private String cambiarEstado(UsuarioAutenticado actual, Long id, boolean activo, RedirectAttributes redirect) {
        try {
            usuarioService.cambiarEstado(actual.empresaId(), id, activo, actual.id());
            redirect.addFlashAttribute("exito", activo ? "El usuario quedó activo." : "El usuario quedó desactivado.");
        } catch (NegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/usuarios";
    }
}
