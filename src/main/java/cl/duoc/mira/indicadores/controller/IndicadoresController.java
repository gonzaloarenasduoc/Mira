package cl.duoc.mira.indicadores.controller;

import cl.duoc.mira.indicadores.service.IndicadoresService;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/** Panel de indicadores (E009). Lo consultan todos los roles (matriz de permisos). */
@Controller
@RequestMapping("/indicadores")
@RequiredArgsConstructor
public class IndicadoresController {

    private final IndicadoresService indicadoresService;

    @ModelAttribute("seccion")
    public String seccion() {
        return "Indicadores";
    }

    @GetMapping
    public String panel(@AuthenticationPrincipal UsuarioAutenticado actual, Model model) {
        model.addAttribute("vencidos", indicadoresService.retornosVencidos(actual.empresaId()));
        model.addAttribute("inventario", indicadoresService.inventario(actual.empresaId()));
        return "indicadores/panel";
    }
}
