package cl.duoc.mira.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Página de inicio de cada rol: accesos a las opciones que el menú le muestra. */
@Controller
public class InicioController {

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("seccion", "Inicio");
        return "inicio";
    }
}
