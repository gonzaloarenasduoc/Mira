package cl.duoc.mira.config;

import static cl.duoc.mira.usuarios.entity.RolUsuario.ADMINISTRADOR;
import static cl.duoc.mira.usuarios.entity.RolUsuario.ENCARGADO_BODEGA;
import static cl.duoc.mira.usuarios.entity.RolUsuario.JEFATURA;
import static cl.duoc.mira.usuarios.entity.RolUsuario.VENDEDOR;

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
            new OpcionMenu("Disponibilidad", "/disponibilidad", EnumSet.allOf(RolUsuario.class)),
            new OpcionMenu("Arriendos", "/arriendos", EnumSet.allOf(RolUsuario.class)),
            new OpcionMenu("Ventas", "/ventas/nueva", EnumSet.of(ADMINISTRADOR, VENDEDOR)),
            new OpcionMenu("Equipos", "/equipos", EnumSet.allOf(RolUsuario.class)),
            new OpcionMenu("Traslados", "/traslados", EnumSet.allOf(RolUsuario.class)),
            new OpcionMenu("Clientes", "/clientes", EnumSet.allOf(RolUsuario.class)),
            new OpcionMenu("Bodegas", "/bodegas", EnumSet.allOf(RolUsuario.class)),
            new OpcionMenu("Técnicos", "/tecnicos", EnumSet.of(ADMINISTRADOR, VENDEDOR, JEFATURA)),
            new OpcionMenu("Indicadores", "/indicadores", EnumSet.allOf(RolUsuario.class)),
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

    /** E006: registran ventas el administrador y el vendedor; la jefatura solo consulta. */
    @ModelAttribute("puedeVender")
    public boolean puedeVender(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario != null && usuario.tieneRol(ADMINISTRADOR, VENDEDOR);
    }

    /** E006: el encargado de bodega no tiene acceso a las ventas. */
    @ModelAttribute("puedeVerVentas")
    public boolean puedeVerVentas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario != null && usuario.tieneRol(ADMINISTRADOR, VENDEDOR, JEFATURA);
    }

    /** E007: agendan capacitaciones y administran técnicos el administrador y el vendedor. */
    @ModelAttribute("puedeAgendar")
    public boolean puedeAgendar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario != null && usuario.tieneRol(ADMINISTRADOR, VENDEDOR);
    }

    @ModelAttribute("puedeArrendar")
    public boolean puedeArrendar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario != null && usuario.tieneRol(ADMINISTRADOR, ENCARGADO_BODEGA, VENDEDOR);
    }
}
