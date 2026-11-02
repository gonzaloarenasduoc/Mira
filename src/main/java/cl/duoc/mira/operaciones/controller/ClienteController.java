package cl.duoc.mira.operaciones.controller;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.operaciones.dto.ClienteResponse;
import cl.duoc.mira.operaciones.dto.GuardarClienteRequest;
import cl.duoc.mira.operaciones.service.ClienteService;
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

/** Administración de clientes (HU-29). Consultan todos los roles; editan los que registran operaciones. */
@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @ModelAttribute("seccion")
    public String seccion() {
        return "Clientes";
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado actual, Model model) {
        model.addAttribute("clientes", clienteService.listar(actual.empresaId()));
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new GuardarClienteRequest());
        return "clientes/formulario";
    }

    @PostMapping
    public String crear(@AuthenticationPrincipal UsuarioAutenticado actual,
                        @Valid @ModelAttribute("cliente") GuardarClienteRequest cliente,
                        BindingResult errores, RedirectAttributes redirect) {
        if (cliente.getRut() == null || cliente.getRut().isBlank()) {
            errores.rejectValue("rut", "requerido", "Ingrese el RUT.");
        }
        if (errores.hasErrors()) {
            return "clientes/formulario";
        }
        try {
            ClienteResponse creado = clienteService.crear(actual.empresaId(), cliente);
            redirect.addFlashAttribute("exito", "Se registró el cliente " + creado.razonSocial() + " (" + creado.rut() + ").");
            return "redirect:/clientes";
        } catch (NegocioException ex) {
            errores.reject("negocio", ex.getMessage());
            return "clientes/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        ClienteResponse c = clienteService.buscar(actual.empresaId(), id);
        model.addAttribute("cliente", new GuardarClienteRequest(c.rut(), c.razonSocial(), c.giro(), c.direccion(),
                c.comuna(), c.contactoNombre(), c.contactoEmail(), c.contactoTelefono()));
        model.addAttribute("id", id);
        return "clientes/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                             @Valid @ModelAttribute("cliente") GuardarClienteRequest cliente,
                             BindingResult errores, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (errores.hasErrors()) {
            return "clientes/formulario";
        }
        try {
            clienteService.actualizar(actual.empresaId(), id, cliente);
            redirect.addFlashAttribute("exito", "Se actualizaron los datos del cliente.");
            return "redirect:/clientes";
        } catch (NegocioException ex) {
            errores.reject("negocio", ex.getMessage());
            return "clientes/formulario";
        }
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                             RedirectAttributes redirect) {
        clienteService.cambiarEstado(actual.empresaId(), id, false);
        redirect.addFlashAttribute("exito", "El cliente quedó inactivo: ya no se ofrece al registrar operaciones.");
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/activar")
    public String activar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                          RedirectAttributes redirect) {
        clienteService.cambiarEstado(actual.empresaId(), id, true);
        redirect.addFlashAttribute("exito", "El cliente quedó activo.");
        return "redirect:/clientes";
    }
}
