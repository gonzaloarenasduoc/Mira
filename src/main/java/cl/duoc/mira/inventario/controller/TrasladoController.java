package cl.duoc.mira.inventario.controller;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.inventario.dto.FiltroTraslados;
import cl.duoc.mira.inventario.dto.RegistrarTrasladoRequest;
import cl.duoc.mira.inventario.dto.TrasladoResponse;
import cl.duoc.mira.inventario.service.BodegaService;
import cl.duoc.mira.inventario.service.TrasladoService;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Registro de traslados y su historial (HU-09 y HU-10). */
@Controller
@RequestMapping("/traslados")
@RequiredArgsConstructor
public class TrasladoController {

    private final TrasladoService trasladoService;
    private final BodegaService bodegaService;

    @ModelAttribute("seccion")
    public String seccion() {
        return "Traslados";
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado actual,
                         @ModelAttribute("filtro") FiltroTraslados filtro,
                         @RequestParam(defaultValue = "0") int pagina, Model model) {
        Page<TrasladoResponse> traslados = Page.empty();
        try {
            traslados = trasladoService.historial(actual.empresaId(), filtro, pagina);
        } catch (NegocioException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        model.addAttribute("traslados", traslados);
        model.addAttribute("enTransito", trasladoService.enTransito(actual.empresaId()));
        model.addAttribute("bodegas", bodegaService.listar(actual.empresaId()));
        return "traslados/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@AuthenticationPrincipal UsuarioAutenticado actual, @RequestParam Long equipoId,
                        Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("preparacion", trasladoService.preparar(actual.empresaId(), equipoId));
        } catch (NegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/equipos/" + equipoId;
        }
        RegistrarTrasladoRequest traslado = new RegistrarTrasladoRequest();
        traslado.setEquipoId(equipoId);
        model.addAttribute("traslado", traslado);
        return "traslados/formulario";
    }

    @PostMapping
    public String registrar(@AuthenticationPrincipal UsuarioAutenticado actual,
                            @Valid @ModelAttribute("traslado") RegistrarTrasladoRequest traslado,
                            BindingResult errores, Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                TrasladoResponse salida = trasladoService.registrarSalida(actual.empresaId(), actual.id(), traslado);
                redirect.addFlashAttribute("exito", "El equipo " + salida.numeroSerie() + " salió de "
                        + salida.bodegaOrigen() + " hacia " + salida.bodegaDestino()
                        + ". Queda en tránsito hasta que se confirme su llegada.");
                return "redirect:/equipos/" + salida.equipoId();
            } catch (NegocioException ex) {
                errores.reject("negocio", ex.getMessage());
            }
        }
        try {
            model.addAttribute("preparacion", trasladoService.preparar(actual.empresaId(), traslado.getEquipoId()));
        } catch (NegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return traslado.getEquipoId() == null ? "redirect:/equipos" : "redirect:/equipos/" + traslado.getEquipoId();
        }
        return "traslados/formulario";
    }

    /** Desde el listado vuelve al listado; desde la ficha, a la ficha del equipo. */
    @PostMapping("/llegada")
    public String confirmarLlegada(@AuthenticationPrincipal UsuarioAutenticado actual, @RequestParam Long equipoId,
                                   @RequestParam(defaultValue = "false") boolean volverAlListado,
                                   RedirectAttributes redirect) {
        try {
            TrasladoResponse llegada = trasladoService.confirmarLlegada(actual.empresaId(), actual.id(), equipoId);
            redirect.addFlashAttribute("exito", "Se confirmó la llegada del equipo " + llegada.numeroSerie()
                    + " a " + llegada.bodegaDestino() + ". Quedó disponible en esa bodega.");
        } catch (NegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return volverAlListado ? "redirect:/traslados" : "redirect:/equipos/" + equipoId;
    }
}
