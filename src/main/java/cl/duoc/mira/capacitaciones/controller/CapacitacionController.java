package cl.duoc.mira.capacitaciones.controller;

import cl.duoc.mira.capacitaciones.dto.AgendarCapacitacionRequest;
import cl.duoc.mira.capacitaciones.dto.AvisoConflicto;
import cl.duoc.mira.capacitaciones.dto.CapacitacionResponse;
import cl.duoc.mira.capacitaciones.service.CapacitacionService;
import cl.duoc.mira.capacitaciones.service.ConflictoTecnicoException;
import cl.duoc.mira.capacitaciones.service.TecnicoService;
import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.operaciones.dto.ArriendoResponse;
import cl.duoc.mira.operaciones.dto.VentaResponse;
import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.operaciones.service.VentaService;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Agendar la capacitación de un arriendo o de una venta (HU-20), con aviso de técnico ocupado (HU-21). Solo el administrador y el vendedor
 * (matriz de E007): las rutas tienen reglas propias en SeguridadConfig, porque el encargado de bodega
 * sí entra a la ficha del arriendo.
 */
@Controller
@RequiredArgsConstructor
public class CapacitacionController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy 'a las' HH:mm");

    private final CapacitacionService capacitacionService;
    private final TecnicoService tecnicoService;
    private final ArriendoService arriendoService;
    private final VentaService ventaService;
    private final Clock clock;

    @GetMapping("/arriendos/{id}/capacitaciones/nueva")
    public String nuevaParaArriendo(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        cargarArriendo(actual, id, model);
        model.addAttribute("capacitacion", nueva());
        return "capacitaciones/formulario";
    }

    @PostMapping("/arriendos/{id}/capacitaciones")
    public String agendarParaArriendo(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                                      @Valid @ModelAttribute("capacitacion") AgendarCapacitacionRequest capacitacion,
                                      BindingResult errores, Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                CapacitacionResponse c = capacitacionService.agendarParaArriendo(actual.empresaId(), id, capacitacion);
                redirect.addFlashAttribute("exito", mensaje(c));
                return "redirect:/arriendos/" + id;
            } catch (ConflictoTecnicoException ex) {
                model.addAttribute("conflicto", ex.getMessage());
            } catch (NegocioException ex) {
                errores.reject("negocio", ex.getMessage());
            }
        }
        cargarArriendo(actual, id, model);
        return "capacitaciones/formulario";
    }

    @GetMapping("/ventas/{id}/capacitaciones/nueva")
    public String nuevaParaVenta(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        cargarVenta(actual, id, model);
        model.addAttribute("capacitacion", nueva());
        return "capacitaciones/formulario";
    }

    @PostMapping("/ventas/{id}/capacitaciones")
    public String agendarParaVenta(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                                   @Valid @ModelAttribute("capacitacion") AgendarCapacitacionRequest capacitacion,
                                   BindingResult errores, Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                CapacitacionResponse c = capacitacionService.agendarParaVenta(actual.empresaId(), id, capacitacion);
                redirect.addFlashAttribute("exito", mensaje(c));
                return "redirect:/ventas/" + id;
            } catch (ConflictoTecnicoException ex) {
                model.addAttribute("conflicto", ex.getMessage());
            } catch (NegocioException ex) {
                errores.reject("negocio", ex.getMessage());
            }
        }
        cargarVenta(actual, id, model);
        return "capacitaciones/formulario";
    }

    /** HU-21: el formulario lo consulta al elegir técnico y fecha, antes de enviar. */
    @GetMapping("/capacitaciones/conflictos")
    @ResponseBody
    public AvisoConflicto conflictos(@AuthenticationPrincipal UsuarioAutenticado actual, @RequestParam Long tecnicoId,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return new AvisoConflicto(capacitacionService.avisoConflicto(actual.empresaId(), tecnicoId, fecha));
    }

    /** Propone mañana a las 10:00; el vendedor lo ajusta. */
    private AgendarCapacitacionRequest nueva() {
        AgendarCapacitacionRequest c = new AgendarCapacitacionRequest();
        c.setFecha(LocalDate.now(clock).plusDays(1));
        c.setHora(LocalTime.of(10, 0));
        return c;
    }

    private void cargarArriendo(UsuarioAutenticado actual, Long id, Model model) {
        ArriendoResponse a = arriendoService.ficha(actual.empresaId(), id);
        model.addAttribute("seccion", "Arriendos");
        model.addAttribute("operacion", "Arriendo N° " + a.id() + " · " + a.cliente());
        model.addAttribute("equipos", a.lineas().stream().map(l -> l.numeroSerie() + " · " + l.equipo()).toList());
        model.addAttribute("volver", "/arriendos/" + id);
        model.addAttribute("accion", "/arriendos/" + id + "/capacitaciones");
        model.addAttribute("tecnicos", tecnicoService.listarActivos(actual.empresaId()));
    }

    private void cargarVenta(UsuarioAutenticado actual, Long id, Model model) {
        VentaResponse v = ventaService.ficha(actual.empresaId(), id);
        model.addAttribute("seccion", "Ventas");
        model.addAttribute("operacion", "Venta N° " + v.id() + " · " + v.cliente());
        model.addAttribute("equipos", v.lineas().stream().map(l -> l.numeroSerie() + " · " + l.equipo()).toList());
        model.addAttribute("volver", "/ventas/" + id);
        model.addAttribute("accion", "/ventas/" + id + "/capacitaciones");
        model.addAttribute("tecnicos", tecnicoService.listarActivos(actual.empresaId()));
    }

    private static String mensaje(CapacitacionResponse c) {
        return "Se agendó la capacitación con " + c.tecnico() + " para el " + FECHA.format(c.fechaProgramada()) + ".";
    }
}
