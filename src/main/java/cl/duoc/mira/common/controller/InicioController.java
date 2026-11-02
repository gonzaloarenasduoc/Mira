package cl.duoc.mira.common.controller;

import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página de inicio: accesos según el rol y las alertas de retorno de equipos arrendados (HU-16).
 * Las alertas se ven al entrar, sin buscarlas; el panel de indicadores (E009) las reutilizará.
 */
@Controller
@RequiredArgsConstructor
public class InicioController {

    private final ArriendoService arriendoService;

    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UsuarioAutenticado actual, Model model) {
        model.addAttribute("seccion", "Inicio");
        model.addAttribute("alertas", arriendoService.alertasDeRetorno(actual.empresaId()));
        return "inicio";
    }
}
