package cl.duoc.mira.config;

import static cl.duoc.mira.usuarios.entity.RolUsuario.ADMINISTRADOR;
import static cl.duoc.mira.usuarios.entity.RolUsuario.ENCARGADO_BODEGA;

import cl.duoc.mira.usuarios.entity.RolUsuario;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Menú y permisos de pantalla según el rol (HU-03). Cada opción declara qué roles la ven,
 * en coherencia con las reglas de SeguridadConfig.
 */
@ControllerAdvice
public class MenuGlobal {

    public record OpcionMenu(String etiqueta, String url, Set<RolUsuario> roles) {
    }

    static final List<OpcionMenu> OPCIONES = List.of(
            new OpcionMenu("Inicio", "/", EnumSet.allOf(RolUsuario.class)),
            new OpcionMenu("Equipos", "/equipos", EnumSet.allOf(RolUsuario.class)),
            new OpcionMenu("Traslados", "/traslados", EnumSet.allOf(RolUsuario.class)),
            new OpcionMenu("Bodegas", "/bodegas", EnumSet.allOf(RolUsuario.class)),
            new OpcionMenu("Usuarios", "/usuarios", EnumSet.of(ADMINISTRADOR)));

    public static List<OpcionMenu> opcionesPara(RolUsuario rol) {
        return OPCIONES.stream().filter(o -> o.roles().contains(rol)).toList();
    }

    @ModelAttribute("menu")
    public List<OpcionMenu> menu(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario == null ? List.of() : opcionesPara(usuario.rol());
    }

    @ModelAttribute("usuarioActual")
    public UsuarioAutenticado usuarioActual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario;
    }

    @ModelAttribute("puedeEditarInventario")
    public boolean puedeEditarInventario(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario != null && usuario.tieneRol(ADMINISTRADOR, ENCARGADO_BODEGA);
    }
}
