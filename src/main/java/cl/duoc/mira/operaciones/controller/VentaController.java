package cl.duoc.mira.operaciones.controller;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.operaciones.dto.LineaVentaRequest;
import cl.duoc.mira.operaciones.dto.RegistrarVentaRequest;
import cl.duoc.mira.operaciones.dto.VentaResponse;
import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.operaciones.service.ClienteService;
import cl.duoc.mira.operaciones.service.VentaService;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Registro de ventas y su ficha (HU-18). */
@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final ArriendoService arriendoService;
    private final ClienteService clienteService;
    private final Clock clock;

    @ModelAttribute("seccion")
    public String seccion() {
        return "Ventas";
    }

    /** Ofrece los equipos disponibles; si viene desde la ficha de un equipo, lo deja marcado. */
    @GetMapping("/nueva")
    public String nueva(@AuthenticationPrincipal UsuarioAutenticado actual,
                        @RequestParam(required = false) Long equipoId, Model model) {
        List<EquipoResponse> disponibles = arriendoService.equiposDisponibles(actual.empresaId());
        RegistrarVentaRequest venta = new RegistrarVentaRequest();
        venta.setFecha(LocalDate.now(clock));
        venta.setLineas(disponibles.stream()
                .map(e -> new LineaVentaRequest(e.id(), e.id().equals(equipoId), null))
                .toList());
        model.addAttribute("venta", venta);
        cargarOpciones(actual, disponibles, model);
        return "ventas/formulario";
    }

    @PostMapping
    public String registrar(@AuthenticationPrincipal UsuarioAutenticado actual,
                            @Valid @ModelAttribute("venta") RegistrarVentaRequest venta,
                            BindingResult errores, Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                VentaResponse registrada = ventaService.registrar(actual.empresaId(), actual.id(), venta);
                redirect.addFlashAttribute("exito", "Se registró la venta a " + registrada.cliente() + " con "
                        + registrada.lineas().size() + (registrada.lineas().size() == 1 ? " equipo." : " equipos."));
                return "redirect:/ventas/" + registrada.id();
            } catch (NegocioException ex) {
                errores.reject("negocio", ex.getMessage());
            }
        }
        cargarOpciones(actual, arriendoService.equiposDisponibles(actual.empresaId()), model);
        return "ventas/formulario";
    }

    @GetMapping("/{id}")
    public String ficha(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        model.addAttribute("venta", ventaService.ficha(actual.empresaId(), id));
        return "ventas/ficha";
    }

    private void cargarOpciones(UsuarioAutenticado actual, List<EquipoResponse> disponibles, Model model) {
        Map<Long, EquipoResponse> equipos = disponibles.stream()
                .collect(Collectors.toMap(EquipoResponse::id, Function.identity()));
        model.addAttribute("equipos", equipos);
        model.addAttribute("clientes", clienteService.listarActivos(actual.empresaId()));
    }
}
