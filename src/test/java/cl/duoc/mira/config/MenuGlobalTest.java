package cl.duoc.mira.config;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.mira.config.MenuGlobal.OpcionMenu;
import cl.duoc.mira.usuarios.entity.RolUsuario;
import org.junit.jupiter.api.Test;

/** HU-03: cada rol ve en el menú solo las opciones de la matriz de permisos. */
class MenuGlobalTest {

    @Test
    void opcionesPara_administrador_incluyeUsuarios() {
        assertThat(MenuGlobal.opcionesPara(RolUsuario.ADMINISTRADOR))
                .extracting(OpcionMenu::etiqueta)
                .containsExactly("Inicio", "Disponibilidad", "Arriendos", "Ventas", "Equipos", "Traslados", "Clientes", "Bodegas", "Usuarios");
    }

    /** HU-17: la jefatura consulta el listado de arriendos (no los registra) y no ve Usuarios. */
    @Test
    void opcionesPara_jefatura_incluyeArriendosPeroNoUsuarios() {
        assertThat(MenuGlobal.opcionesPara(RolUsuario.JEFATURA))
                .extracting(OpcionMenu::etiqueta)
                .containsExactly("Inicio", "Disponibilidad", "Arriendos", "Equipos", "Traslados", "Clientes", "Bodegas");
    }

    @Test
    void opcionesPara_vendedor_incluyeVentasPeroNoUsuarios() {
        assertThat(MenuGlobal.opcionesPara(RolUsuario.VENDEDOR))
                .extracting(OpcionMenu::etiqueta)
                .containsExactly("Inicio", "Disponibilidad", "Arriendos", "Ventas", "Equipos", "Traslados", "Clientes", "Bodegas");
    }

    /** E006: el encargado de bodega no tiene acceso a las ventas. */
    @Test
    void opcionesPara_encargadoDeBodega_noIncluyeVentasNiUsuarios() {
        assertThat(MenuGlobal.opcionesPara(RolUsuario.ENCARGADO_BODEGA))
                .extracting(OpcionMenu::etiqueta)
                .containsExactly("Inicio", "Disponibilidad", "Arriendos", "Equipos", "Traslados", "Clientes", "Bodegas");
    }
}
