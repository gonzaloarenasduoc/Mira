package cl.duoc.mira.indicadores.controller;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.indicadores.service.IndicadoresService;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    /** Sin fechas, la utilización usa los últimos 30 días; un periodo inválido se avisa y se usa el de omisión. */
    @GetMapping
    public String panel(@AuthenticationPrincipal UsuarioAutenticado actual,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                        Model model) {
        model.addAttribute("vencidos", indicadoresService.retornosVencidos(actual.empresaId()));
        model.addAttribute("inventario", indicadoresService.inventario(actual.empresaId()));
        try {
            model.addAttribute("utilizacion", indicadoresService.utilizacion(actual.empresaId(), desde, hasta));
        } catch (NegocioException ex) {
            model.addAttribute("errorPeriodo", ex.getMessage());
            model.addAttribute("utilizacion", indicadoresService.utilizacion(actual.empresaId(), null, null));
        }
        model.addAttribute("vigentes", indicadoresService.arriendosVigentes(actual.empresaId()));
        return "indicadores/panel";
    }
}
