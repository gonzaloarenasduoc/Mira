package cl.duoc.mira.usuarios.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** El formulario lo procesa Spring Security; aquí solo se muestra la vista (HU-01). */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
