package cl.duoc.mira.inventario.controller;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.inventario.dto.BodegaResponse;
import cl.duoc.mira.inventario.dto.GuardarBodegaRequest;
import cl.duoc.mira.inventario.service.BodegaService;
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

/** Administración de bodegas (HU-08). Consultan todos los roles; editan administrador y encargado. */
@Controller
@RequestMapping("/bodegas")
@RequiredArgsConstructor
public class BodegaController {

    private final BodegaService bodegaService;

    @ModelAttribute("seccion")
    public String seccion() {
        return "Bodegas";
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado actual, Model model) {
        model.addAttribute("bodegas", bodegaService.listar(actual.empresaId()));
        return "bodegas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("bodega", new GuardarBodegaRequest());
        return "bodegas/formulario";
    }

    @PostMapping
    public String crear(@AuthenticationPrincipal UsuarioAutenticado actual,
                        @Valid @ModelAttribute("bodega") GuardarBodegaRequest bodega,
                        BindingResult errores, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return "bodegas/formulario";
        }
        try {
            BodegaResponse creada = bodegaService.crear(actual.empresaId(), bodega);
            redirect.addFlashAttribute("exito", "Se creó la bodega " + creada.nombre() + ".");
            return "redirect:/bodegas";
        } catch (NegocioException ex) {
            errores.reject("negocio", ex.getMessage());
            return "bodegas/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        BodegaResponse b = bodegaService.buscar(actual.empresaId(), id);
        model.addAttribute("bodega", new GuardarBodegaRequest(b.nombre(), b.direccion(), b.comuna()));
        model.addAttribute("id", id);
        return "bodegas/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                             @Valid @ModelAttribute("bodega") GuardarBodegaRequest bodega,
                             BindingResult errores, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (errores.hasErrors()) {
            return "bodegas/formulario";
        }
        try {
            bodegaService.actualizar(actual.empresaId(), id, bodega);
            redirect.addFlashAttribute("exito", "Se actualizaron los datos de la bodega.");
            return "redirect:/bodegas";
        } catch (NegocioException ex) {
            errores.reject("negocio", ex.getMessage());
            return "bodegas/formulario";
        }
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                             RedirectAttributes redirect) {
        bodegaService.cambiarEstado(actual.empresaId(), id, false);
        redirect.addFlashAttribute("exito", "La bodega quedó inactiva: ya no admite nuevos equipos.");
        return "redirect:/bodegas";
    }

    @PostMapping("/{id}/activar")
    public String activar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                          RedirectAttributes redirect) {
        bodegaService.cambiarEstado(actual.empresaId(), id, true);
        redirect.addFlashAttribute("exito", "La bodega quedó activa.");
        return "redirect:/bodegas";
    }
}
