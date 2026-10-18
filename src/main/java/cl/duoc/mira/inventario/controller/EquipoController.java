package cl.duoc.mira.inventario.controller;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.dto.FiltroEquipos;
import cl.duoc.mira.inventario.dto.RegistrarEquipoRequest;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.service.BodegaService;
import cl.duoc.mira.inventario.service.CategoriaService;
import cl.duoc.mira.inventario.service.EquipoService;
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

/** Registro, listado y ficha de equipos (HU-04, HU-05 y HU-07). */
@Controller
@RequestMapping("/equipos")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;
    private final CategoriaService categoriaService;
    private final BodegaService bodegaService;

    @ModelAttribute("seccion")
    public String seccion() {
        return "Equipos";
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado actual,
                         @ModelAttribute("filtro") FiltroEquipos filtro, Model model) {
        model.addAttribute("equipos", equipoService.buscar(actual.empresaId(), filtro));
        model.addAttribute("categorias", categoriaService.listarActivas(actual.empresaId()));
        model.addAttribute("bodegas", bodegaService.listar(actual.empresaId()));
        model.addAttribute("estados", EstadoEquipo.values());
        return "equipos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@AuthenticationPrincipal UsuarioAutenticado actual, Model model) {
        model.addAttribute("equipo", new RegistrarEquipoRequest());
        cargarOpciones(actual, model);
        return "equipos/formulario";
    }

    @PostMapping
    public String registrar(@AuthenticationPrincipal UsuarioAutenticado actual,
                            @Valid @ModelAttribute("equipo") RegistrarEquipoRequest equipo,
                            BindingResult errores, Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                EquipoResponse registrado = equipoService.registrar(actual.empresaId(), actual.id(), equipo);
                redirect.addFlashAttribute("exito",
                        "Se registró el equipo " + registrado.numeroSerie() + " en la bodega " + registrado.bodega() + ".");
                return "redirect:/equipos/" + registrado.id();
            } catch (NegocioException ex) {
                errores.reject("negocio", ex.getMessage());
            }
        }
        cargarOpciones(actual, model);
        return "equipos/formulario";
    }

    @GetMapping("/{id}")
    public String ficha(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        model.addAttribute("ficha", equipoService.ficha(actual.empresaId(), id));
        return "equipos/ficha";
    }

    private void cargarOpciones(UsuarioAutenticado actual, Model model) {
        model.addAttribute("categorias", categoriaService.listarActivas(actual.empresaId()));
        model.addAttribute("bodegas", bodegaService.listarActivas(actual.empresaId()));
    }
}
