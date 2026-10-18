package cl.duoc.mira.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.mira.common.controller.InicioController;
import cl.duoc.mira.inventario.controller.BodegaController;
import cl.duoc.mira.inventario.controller.EquipoController;
import cl.duoc.mira.inventario.controller.TrasladoController;
import cl.duoc.mira.inventario.service.BodegaService;
import cl.duoc.mira.inventario.service.CategoriaService;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.inventario.service.TrasladoService;
import cl.duoc.mira.usuarios.controller.UsuarioController;
import cl.duoc.mira.usuarios.entity.RolUsuario;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import cl.duoc.mira.usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Permisos por rol según la matriz de docs/01-contexto-negocio.md. Levanta solo la capa web,
 * con los servicios simulados: no necesita base de datos.
 */
@WebMvcTest(controllers = {InicioController.class, UsuarioController.class,
        BodegaController.class, EquipoController.class, TrasladoController.class})
@Import({SeguridadConfig.class, MenuGlobal.class})
class SeguridadTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsuarioService usuarioService;
    @MockitoBean
    private BodegaService bodegaService;
    @MockitoBean
    private CategoriaService categoriaService;
    @MockitoBean
    private EquipoService equipoService;
    @MockitoBean
    private TrasladoService trasladoService;

    /** PNF-05: toda URL protegida, accedida sin sesión, redirige al inicio de sesión. */
    @ParameterizedTest
    @ValueSource(strings = {"/", "/equipos", "/equipos/nuevo", "/bodegas", "/bodegas/nueva", "/usuarios",
            "/traslados", "/traslados/nuevo?equipoId=1"})
    void rutaProtegida_sinSesion_redirigeAlLogin(String ruta) throws Exception {
        mvc.perform(get(ruta))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    /** CF-11: un vendedor no accede a la administración de usuarios. */
    @Test
    void usuarios_comoVendedor_accesoDenegado() throws Exception {
        mvc.perform(get("/usuarios").with(user(con(RolUsuario.VENDEDOR))))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarios_comoAdministrador_permiteElAcceso() throws Exception {
        mvc.perform(get("/usuarios").with(user(con(RolUsuario.ADMINISTRADOR))))
                .andExpect(status().isOk());
    }

    /** E002: vendedor y jefatura consultan el inventario, pero no lo modifican. */
    @Test
    void equipos_comoVendedor_puedeConsultar() throws Exception {
        mvc.perform(get("/equipos").with(user(con(RolUsuario.VENDEDOR))))
                .andExpect(status().isOk());
    }

    @Test
    void registrarEquipo_comoJefatura_accesoDenegado() throws Exception {
        mvc.perform(get("/equipos/nuevo").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearBodega_comoVendedor_accesoDenegado() throws Exception {
        mvc.perform(post("/bodegas").param("nombre", "Bodega Sur")
                        .with(csrf()).with(user(con(RolUsuario.VENDEDOR))))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarEquipo_comoEncargadoDeBodega_permiteElAcceso() throws Exception {
        mvc.perform(get("/equipos/nuevo").with(user(con(RolUsuario.ENCARGADO_BODEGA))))
                .andExpect(status().isOk());
    }

    /** E003: el vendedor consulta los traslados, pero no los registra ni confirma llegadas. */
    @Test
    void traslados_comoVendedor_puedeConsultar() throws Exception {
        when(trasladoService.historial(any(), any(), anyInt())).thenReturn(Page.empty());
        mvc.perform(get("/traslados").with(user(con(RolUsuario.VENDEDOR))))
                .andExpect(status().isOk());
    }

    @Test
    void registrarTraslado_comoVendedor_accesoDenegado() throws Exception {
        mvc.perform(post("/traslados").param("equipoId", "1").param("bodegaDestinoId", "2")
                        .with(csrf()).with(user(con(RolUsuario.VENDEDOR))))
                .andExpect(status().isForbidden());
    }

    @Test
    void confirmarLlegada_comoJefatura_accesoDenegado() throws Exception {
        mvc.perform(post("/traslados/llegada").param("equipoId", "1")
                        .with(csrf()).with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
    }

    private static UsuarioAutenticado con(RolUsuario rol) {
        return new UsuarioAutenticado(1L, 1L, "prueba@mira.cl", "{noop}x", "Usuario Prueba", rol, true);
    }
}
