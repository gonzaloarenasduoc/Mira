package cl.duoc.mira.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.mira.common.controller.InicioController;
import cl.duoc.mira.documentos.controller.DocumentoController;
import cl.duoc.mira.documentos.service.DocumentoService;
import cl.duoc.mira.operaciones.dto.ArriendoResponse;
import cl.duoc.mira.inventario.controller.BodegaController;
import cl.duoc.mira.inventario.controller.EquipoController;
import cl.duoc.mira.inventario.controller.TrasladoController;
import cl.duoc.mira.inventario.service.BodegaService;
import cl.duoc.mira.inventario.service.CategoriaService;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.inventario.service.TrasladoService;
import cl.duoc.mira.operaciones.controller.ArriendoController;
import cl.duoc.mira.operaciones.controller.ClienteController;
import cl.duoc.mira.operaciones.controller.DisponibilidadController;
import cl.duoc.mira.operaciones.controller.VentaController;
import cl.duoc.mira.operaciones.dto.AlertaRetornoResponse;
import cl.duoc.mira.operaciones.dto.DisponibilidadResponse;
import cl.duoc.mira.operaciones.dto.EstadoArriendo;
import cl.duoc.mira.operaciones.dto.ResumenArriendoResponse;
import cl.duoc.mira.operaciones.dto.VentaResponse;
import cl.duoc.mira.operaciones.dto.OperacionClienteResponse;
import cl.duoc.mira.operaciones.dto.ClienteResponse;
import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.operaciones.service.ClienteService;
import cl.duoc.mira.operaciones.service.DisponibilidadService;
import cl.duoc.mira.operaciones.service.VentaService;
import cl.duoc.mira.operaciones.service.HistorialClienteService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Permisos por rol según la matriz de docs/01-contexto-negocio.md. Levanta solo la capa web,
 * con los servicios simulados: no necesita base de datos.
 */
@WebMvcTest(controllers = {InicioController.class, UsuarioController.class,
        BodegaController.class, EquipoController.class, TrasladoController.class,
        DisponibilidadController.class, ArriendoController.class, ClienteController.class, VentaController.class,
        DocumentoController.class})
@Import({SeguridadConfig.class, MenuGlobal.class, RelojConfig.class})
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
    @MockitoBean
    private DisponibilidadService disponibilidadService;
    @MockitoBean
    private ArriendoService arriendoService;
    @MockitoBean
    private ClienteService clienteService;
    @MockitoBean
    private VentaService ventaService;
    @MockitoBean
    private HistorialClienteService historialClienteService;
    @MockitoBean
    private DocumentoService documentoService;

    /** PNF-05: toda URL protegida, accedida sin sesión, redirige al inicio de sesión. */
    @ParameterizedTest
    @ValueSource(strings = {"/", "/equipos", "/equipos/nuevo", "/bodegas", "/bodegas/nueva", "/usuarios",
            "/traslados", "/traslados/nuevo?equipoId=1", "/disponibilidad",
            "/arriendos", "/arriendos/nuevo", "/arriendos/1/retorno?equipoId=1", "/clientes", "/clientes/nuevo", "/ventas/nueva", "/ventas/1", "/clientes/1", "/arriendos/1/documentos/nuevo", "/ventas/1/documentos/nuevo"})
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

    /** E004: los cuatro roles consultan la disponibilidad. */
    @ParameterizedTest
    @ValueSource(strings = {"ADMINISTRADOR", "ENCARGADO_BODEGA", "VENDEDOR", "JEFATURA"})
    void disponibilidad_cualquierRol_puedeConsultar(String rol) throws Exception {
        when(disponibilidadService.consultar(any(), any()))
                .thenReturn(new DisponibilidadResponse(List.of(), 0, 0, LocalDate.of(2026, 10, 20)));
        mvc.perform(get("/disponibilidad").with(user(con(RolUsuario.valueOf(rol)))))
                .andExpect(status().isOk());
    }

    /** E005: administrador, encargado de bodega y vendedor registran arriendos; la jefatura no. */
    @ParameterizedTest
    @ValueSource(strings = {"ADMINISTRADOR", "ENCARGADO_BODEGA", "VENDEDOR"})
    void registrarArriendo_rolesQueOperan_permiteElAcceso(String rol) throws Exception {
        mvc.perform(get("/arriendos/nuevo").with(user(con(RolUsuario.valueOf(rol)))))
                .andExpect(status().isOk());
    }

    /** CF-10: al entrar, el inicio muestra el arriendo vencido con el cliente, el equipo y los días de atraso. */
    @Test
    void inicio_conUnArriendoVencido_loMuestraDestacado() throws Exception {
        when(arriendoService.alertasDeRetorno(any())).thenReturn(List.of(new AlertaRetornoResponse(4L,
                "Constructora Andes SpA", 3L, "M300-1A2B3C", "Dron DJI Matrice 300 RTK", LocalDate.of(2026, 10, 15), -13)));

        mvc.perform(get("/").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Vencido hace 13 días")))
                .andExpect(content().string(containsString("Constructora Andes SpA")))
                .andExpect(content().string(containsString("M300-1A2B3C")))
                .andExpect(content().string(not(containsString("Registrar retorno"))));
    }

    /** HU-17: la jefatura consulta el listado con el estado y el total, sin el botón para registrar. */
    @Test
    void listadoDeArriendos_comoJefatura_muestraEstadoYTotalSinRegistrar() throws Exception {
        when(arriendoService.listar(any(), eq(0))).thenReturn(new PageImpl<>(List.of(new ResumenArriendoResponse(4L,
                "Constructora Andes SpA", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), List.of("M300-1A2B3C"),
                EstadoArriendo.VENCIDO, new BigDecimal("2400000"), true))));

        mvc.perform(get("/arriendos").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Constructora Andes SpA")))
                .andExpect(content().string(containsString("Vencido")))
                .andExpect(content().string(containsString("$ 2.400.000")))
                .andExpect(content().string(containsString("Estimado")))
                .andExpect(content().string(not(containsString("Registrar arriendo"))));
    }

    @Test
    void listadoDeArriendos_comoVendedor_ofreceRegistrar() throws Exception {
        when(arriendoService.listar(any(), eq(0))).thenReturn(Page.empty());

        mvc.perform(get("/arriendos").with(user(con(RolUsuario.VENDEDOR))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrar arriendo")));
    }

    /** E006: registran ventas el administrador y el vendedor (matriz de permisos). */
    @ParameterizedTest
    @ValueSource(strings = {"ADMINISTRADOR", "VENDEDOR"})
    void registrarVenta_rolesQueVenden_permiteElAcceso(String rol) throws Exception {
        mvc.perform(get("/ventas/nueva").with(user(con(RolUsuario.valueOf(rol)))))
                .andExpect(status().isOk());
    }

    /** E006: la jefatura consulta una venta, pero no la registra. */
    @Test
    void venta_comoJefatura_consultaPeroNoRegistra() throws Exception {
        when(ventaService.ficha(any(), eq(7L))).thenReturn(new VentaResponse(7L, "Constructora Andes SpA",
                "76.123.456-0", LocalDate.of(2026, 11, 9), null, null, List.of(), new BigDecimal("14200000")));

        mvc.perform(get("/ventas/7").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Venta N° 7")))
                .andExpect(content().string(containsString("$ 14.200.000")));
        mvc.perform(get("/ventas/nueva").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/ventas").param("clienteId", "1")
                        .with(csrf()).with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
    }

    /** E006: el encargado de bodega no tiene acceso a las ventas, ni para consultarlas. */
    @Test
    void ventas_comoEncargadoDeBodega_accesoDenegado() throws Exception {
        mvc.perform(get("/ventas/nueva").with(user(con(RolUsuario.ENCARGADO_BODEGA))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/ventas/7").with(user(con(RolUsuario.ENCARGADO_BODEGA))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/ventas").param("clienteId", "1")
                        .with(csrf()).with(user(con(RolUsuario.ENCARGADO_BODEGA))))
                .andExpect(status().isForbidden());
    }

    /** HU-19: la ficha del cliente muestra su historial; con acceso a E006 incluye las ventas. */
    @Test
    void fichaCliente_comoJefatura_muestraVentasYArriendos() throws Exception {
        when(clienteService.buscar(any(), eq(3L))).thenReturn(clienteDePrueba());
        when(historialClienteService.historial(any(), eq(3L), eq(true))).thenReturn(List.of(
                new OperacionClienteResponse(9L, true, LocalDate.of(2026, 11, 9), List.of("TS13-221045"), null,
                        new BigDecimal("12900000"), false),
                new OperacionClienteResponse(1L, false, LocalDate.of(2026, 10, 20), List.of("NA2-55021"),
                        EstadoArriendo.CERRADO, new BigDecimal("195000"), false)));

        mvc.perform(get("/clientes/3").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Historial de operaciones")))
                .andExpect(content().string(containsString("Venta N° 9")))
                .andExpect(content().string(containsString("Arriendo N° 1")))
                .andExpect(content().string(containsString("$ 12.900.000")))
                .andExpect(content().string(not(containsString("Editar"))));
    }

    /** HU-19 y matriz de permisos: el encargado de bodega ve solo los arriendos del cliente. */
    @Test
    void fichaCliente_comoEncargadoDeBodega_noPideLasVentas() throws Exception {
        when(clienteService.buscar(any(), eq(3L))).thenReturn(clienteDePrueba());
        when(historialClienteService.historial(any(), eq(3L), eq(false))).thenReturn(List.of());

        mvc.perform(get("/clientes/3").with(user(con(RolUsuario.ENCARGADO_BODEGA))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Historial de arriendos")))
                .andExpect(content().string(containsString("solo se muestran los arriendos")));
    }

    /** HU-23 y matriz de E008: el encargado de bodega adjunta documentos a un arriendo. */
    @Test
    void adjuntarDocumento_aArriendoComoEncargado_permiteElAcceso() throws Exception {
        when(arriendoService.ficha(any(), eq(1L))).thenReturn(new ArriendoResponse(1L, "Constructora Andes SpA",
                "76.123.456-0", LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 16),
                null, null, List.of(), BigDecimal.ZERO));

        mvc.perform(get("/arriendos/1/documentos/nuevo").with(user(con(RolUsuario.ENCARGADO_BODEGA))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Arriendo N° 1 · Constructora Andes SpA")))
                .andExpect(content().string(containsString("Guía de despacho")));
    }

    /** E008: la jefatura solo consulta; el encargado no adjunta a ventas porque no accede a ellas. */
    @Test
    void adjuntarDocumento_sinPermiso_accesoDenegado() throws Exception {
        mvc.perform(get("/arriendos/1/documentos/nuevo").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/arriendos/1/documentos").with(csrf()).with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/ventas/1/documentos/nuevo").with(user(con(RolUsuario.ENCARGADO_BODEGA))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/ventas/1/documentos").with(csrf()).with(user(con(RolUsuario.ENCARGADO_BODEGA))))
                .andExpect(status().isForbidden());
    }

    /** D17: la jefatura consulta los clientes, pero no los registra ni los modifica. */
    @Test
    void clientes_comoJefatura_consultaPeroNoRegistra() throws Exception {
        mvc.perform(get("/clientes").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isOk());
        mvc.perform(get("/clientes/nuevo").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/clientes").param("rut", "12.345.678-5").param("razonSocial", "X")
                        .with(csrf()).with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarCliente_comoVendedor_permiteElAcceso() throws Exception {
        mvc.perform(get("/clientes/nuevo").with(user(con(RolUsuario.VENDEDOR))))
                .andExpect(status().isOk());
    }

    /** E005: la jefatura tampoco registra retornos. */
    @Test
    void registrarRetorno_comoJefatura_accesoDenegado() throws Exception {
        mvc.perform(get("/arriendos/1/retorno").param("equipoId", "1").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/arriendos/1/retornos").param("equipoId", "1").param("bodegaId", "1")
                        .with(csrf()).with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarArriendo_comoJefatura_accesoDenegado() throws Exception {
        mvc.perform(get("/arriendos/nuevo").with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/arriendos").param("clienteId", "1")
                        .with(csrf()).with(user(con(RolUsuario.JEFATURA))))
                .andExpect(status().isForbidden());
    }

    private static ClienteResponse clienteDePrueba() {
        return new ClienteResponse(3L, "76.123.456-0", "Constructora Los Andes SpA", null, null, "Las Condes",
                null, null, null, true);
    }

    private static UsuarioAutenticado con(RolUsuario rol) {
        return new UsuarioAutenticado(1L, 1L, "prueba@mira.cl", "{noop}x", "Usuario Prueba", rol, true);
    }
}
