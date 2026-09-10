package cl.duoc.mira.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Provisional: pasa a Gonzalo cuando arme el layout real de las vistas.
 * Ver docs/ESTADO.md.
 */
@Controller
public class InicioController {

    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }
}
