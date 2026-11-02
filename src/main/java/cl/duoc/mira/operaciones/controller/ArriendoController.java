package cl.duoc.mira.operaciones.controller;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.inventario.dto.BodegaResponse;
import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.service.BodegaService;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.ArriendoResponse;
import cl.duoc.mira.operaciones.dto.LineaArriendoRequest;
import cl.duoc.mira.operaciones.dto.LineaArriendoResponse;
import cl.duoc.mira.operaciones.dto.RegistrarArriendoRequest;
import cl.duoc.mira.operaciones.dto.RegistrarRetornoRequest;
import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.operaciones.service.ClienteService;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/** Registro de arriendos, su ficha y el retorno de sus equipos (HU-14 y HU-15). */
@Controller
@RequestMapping("/arriendos")
@RequiredArgsConstructor
public class ArriendoController {

    private final ArriendoService arriendoService;
    private final ClienteService clienteService;
    private final BodegaService bodegaService;
    private final EquipoService equipoService;
    private final Clock clock;

    @ModelAttribute("seccion")
    public String seccion() {
        return "Arriendos";
    }

    /** Ofrece todos los equipos disponibles; si viene desde la ficha de un equipo, lo deja marcado. */
    @GetMapping("/nuevo")
    public String nuevo(@AuthenticationPrincipal UsuarioAutenticado actual,
                        @RequestParam(required = false) Long equipoId, Model model) {
        List<EquipoResponse> disponibles = arriendoService.equiposDisponibles(actual.empresaId());
        RegistrarArriendoRequest arriendo = new RegistrarArriendoRequest();
        arriendo.setFechaInicio(LocalDate.now(clock));
        arriendo.setLineas(disponibles.stream()
                .map(e -> new LineaArriendoRequest(e.id(), e.id().equals(equipoId), e.tarifaDiaria()))
                .toList());
        model.addAttribute("arriendo", arriendo);
        cargarOpciones(actual, disponibles, model);
        return "arriendos/formulario";
    }

    @PostMapping
    public String registrar(@AuthenticationPrincipal UsuarioAutenticado actual,
                            @Valid @ModelAttribute("arriendo") RegistrarArriendoRequest arriendo,
                            BindingResult errores, Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                ArriendoResponse registrado = arriendoService.registrar(actual.empresaId(), actual.id(), arriendo);
                redirect.addFlashAttribute("exito", "Se registró el arriendo a " + registrado.cliente() + " con "
                        + registrado.lineas().size() + (registrado.lineas().size() == 1 ? " equipo." : " equipos."));
                return "redirect:/arriendos/" + registrado.id();
            } catch (NegocioException ex) {
                errores.reject("negocio", ex.getMessage());
            }
        }
        cargarOpciones(actual, arriendoService.equiposDisponibles(actual.empresaId()), model);
        return "arriendos/formulario";
    }

    @GetMapping("/{id}")
    public String ficha(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        model.addAttribute("arriendo", arriendoService.ficha(actual.empresaId(), id));
        return "arriendos/ficha";
    }

    /** Propone como bodega receptora la de asignación del equipo y como fecha, la de hoy. */
    @GetMapping("/{id}/retorno")
    public String retorno(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                          @RequestParam Long equipoId, Model model) {
        RegistrarRetornoRequest retorno = new RegistrarRetornoRequest();
        retorno.setEquipoId(equipoId);
        retorno.setFechaRetorno(LocalDate.now(clock));
        List<BodegaResponse> bodegas = bodegaService.listarActivas(actual.empresaId());
        EquipoResponse equipo = equipoService.resumenPorId(actual.empresaId(), Set.of(equipoId)).get(equipoId);
        if (equipo != null) {
            bodegas.stream().filter(b -> b.nombre().equals(equipo.bodega())).findFirst()
                    .ifPresent(b -> retorno.setBodegaId(b.id()));
        }
        model.addAttribute("retorno", retorno);
        return cargarRetorno(actual, id, equipoId, bodegas, model);
    }

    @PostMapping("/{id}/retornos")
    public String registrarRetorno(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                                   @Valid @ModelAttribute("retorno") RegistrarRetornoRequest retorno,
                                   BindingResult errores, Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                ArriendoResponse arriendo = arriendoService.registrarRetorno(actual.empresaId(), actual.id(), id, retorno);
                String serie = arriendo.lineas().stream().filter(l -> l.equipoId().equals(retorno.getEquipoId()))
                        .map(LineaArriendoResponse::numeroSerie).findFirst().orElse("");
                redirect.addFlashAttribute("exito", "Se registró el retorno del equipo " + serie
                        + ". Quedó disponible en la bodega que lo recibió.");
                return "redirect:/arriendos/" + id;
            } catch (NegocioException ex) {
                errores.reject("negocio", ex.getMessage());
            }
        }
        return cargarRetorno(actual, id, retorno.getEquipoId(), bodegaService.listarActivas(actual.empresaId()), model);
    }

    private String cargarRetorno(UsuarioAutenticado actual, Long id, Long equipoId, List<BodegaResponse> bodegas,
                                 Model model) {
        ArriendoResponse arriendo = arriendoService.ficha(actual.empresaId(), id);
        model.addAttribute("arriendo", arriendo);
        model.addAttribute("linea", arriendo.lineas().stream().filter(l -> l.equipoId().equals(equipoId))
                .findFirst().orElse(null));
        model.addAttribute("bodegas", bodegas);
        return "arriendos/retorno";
    }

    private void cargarOpciones(UsuarioAutenticado actual, List<EquipoResponse> disponibles, Model model) {
        Map<Long, EquipoResponse> equipos = disponibles.stream()
                .collect(Collectors.toMap(EquipoResponse::id, Function.identity()));
        model.addAttribute("equipos", equipos);
        model.addAttribute("clientes", clienteService.listarActivos(actual.empresaId()));
    }
}
