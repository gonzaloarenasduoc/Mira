package cl.duoc.mira.capacitaciones.controller;

import cl.duoc.mira.capacitaciones.dto.GuardarTecnicoRequest;
import cl.duoc.mira.capacitaciones.dto.TecnicoResponse;
import cl.duoc.mira.capacitaciones.service.TecnicoService;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
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

/** Administración de técnicos (HU-30). Consultan los roles de E007; editan administrador y vendedor. */
@Controller
@RequestMapping("/tecnicos")
@RequiredArgsConstructor
public class TecnicoController {

    private final TecnicoService tecnicoService;

    @ModelAttribute("seccion")
    public String seccion() {
        return "Técnicos";
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado actual, Model model) {
        model.addAttribute("tecnicos", tecnicoService.listar(actual.empresaId()));
        return "tecnicos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("tecnico", new GuardarTecnicoRequest());
        return "tecnicos/formulario";
    }

    @PostMapping
    public String crear(@AuthenticationPrincipal UsuarioAutenticado actual,
                        @Valid @ModelAttribute("tecnico") GuardarTecnicoRequest tecnico,
                        BindingResult errores, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return "tecnicos/formulario";
        }
        TecnicoResponse creado = tecnicoService.crear(actual.empresaId(), tecnico);
        redirect.addFlashAttribute("exito", "Se registró el técnico " + creado.nombreCompleto() + ".");
        return "redirect:/tecnicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        TecnicoResponse t = tecnicoService.buscar(actual.empresaId(), id);
        model.addAttribute("tecnico", new GuardarTecnicoRequest(t.nombre(), t.apellido(), t.email(), t.telefono()));
        model.addAttribute("id", id);
        return "tecnicos/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                             @Valid @ModelAttribute("tecnico") GuardarTecnicoRequest tecnico,
                             BindingResult errores, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (errores.hasErrors()) {
            return "tecnicos/formulario";
        }
        tecnicoService.actualizar(actual.empresaId(), id, tecnico);
        redirect.addFlashAttribute("exito", "Se actualizaron los datos del técnico.");
        return "redirect:/tecnicos";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                             RedirectAttributes redirect) {
        tecnicoService.cambiarEstado(actual.empresaId(), id, false);
        redirect.addFlashAttribute("exito", "El técnico quedó inactivo: ya no se ofrece al agendar capacitaciones.");
        return "redirect:/tecnicos";
    }

    @PostMapping("/{id}/activar")
    public String activar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                          RedirectAttributes redirect) {
        tecnicoService.cambiarEstado(actual.empresaId(), id, true);
        redirect.addFlashAttribute("exito", "El técnico quedó activo.");
        return "redirect:/tecnicos";
    }
}
