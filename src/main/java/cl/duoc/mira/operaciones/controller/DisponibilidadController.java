package cl.duoc.mira.operaciones.controller;

import cl.duoc.mira.inventario.service.CategoriaService;
import cl.duoc.mira.operaciones.dto.FiltroDisponibilidad;
import cl.duoc.mira.operaciones.service.DisponibilidadService;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/** Consulta de disponibilidad consolidada de todas las bodegas (HU-11, HU-12 y HU-13). */
@Controller
@RequestMapping("/disponibilidad")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;
    private final CategoriaService categoriaService;

    @ModelAttribute("seccion")
    public String seccion() {
        return "Disponibilidad";
    }

    @GetMapping
    public String consultar(@AuthenticationPrincipal UsuarioAutenticado actual,
                            @ModelAttribute("filtro") FiltroDisponibilidad filtro, Model model) {
        model.addAttribute("resultado", disponibilidadService.consultar(actual.empresaId(), filtro));
        model.addAttribute("categorias", categoriaService.listarActivas(actual.empresaId()));
        return "disponibilidad/consulta";
    }
}
