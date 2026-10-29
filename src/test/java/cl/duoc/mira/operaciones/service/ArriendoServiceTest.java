package cl.duoc.mira.operaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.mira.common.entity.Empresa;
import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.service.EquipoNoDisponibleException;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.AlertaRetornoResponse;
import cl.duoc.mira.operaciones.dto.LineaArriendoRequest;
import cl.duoc.mira.operaciones.dto.RegistrarArriendoRequest;
import cl.duoc.mira.operaciones.dto.RegistrarRetornoRequest;
import cl.duoc.mira.operaciones.entity.Arriendo;
import cl.duoc.mira.operaciones.entity.Cliente;
import cl.duoc.mira.operaciones.repository.ArriendoRepository;
import cl.duoc.mira.operaciones.repository.ArriendoVigente;
import cl.duoc.mira.operaciones.repository.LineaPorRetornar;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

/** HU-14: registro de un arriendo validando disponibilidad (CF-01, CF-02 y CF-03). */
class ArriendoServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long USUARIO_ID = 5L;
    private static final Long CLIENTE_ID = 7L;
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-03-10T13:00:00Z"), ZoneId.of("America/Santiago"));

    private ArriendoRepository arriendoRepository;
    private EquipoService equipoService;
    private ArriendoService service;
    private Cliente cliente;

    @BeforeEach
    void preparar() {
        arriendoRepository = mock(ArriendoRepository.class);
        equipoService = mock(EquipoService.class);
        ClienteService clienteService = mock(ClienteService.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        service = new ArriendoService(arriendoRepository, clienteService, equipoService, empresaRepository, RELOJ);

        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(mock(Empresa.class));
        cliente = mock(Cliente.class);
        when(cliente.isActivo()).thenReturn(true);
        when(cliente.getRazonSocial()).thenReturn("Constructora Andes SpA");
        when(clienteService.obtener(EMPRESA_ID, CLIENTE_ID)).thenReturn(cliente);
        when(clienteService.obtener(EMPRESA_ID, 99L)).thenThrow(new RecursoNoEncontradoException("El cliente"));
        when(arriendoRepository.save(any(Arriendo.class))).thenAnswer(inv -> {
            Arriendo arriendo = inv.getArgument(0);
            ReflectionTestUtils.setField(arriendo, "id", 500L);
            return arriendo;
        });
        when(equipoService.resumenPorId(eq(EMPRESA_ID), anyCollection())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(1);
            return ids.stream().filter(id -> id < 90).collect(Collectors.toMap(id -> id, ArriendoServiceTest::equipo));
        });
    }

    /** CF-01: el arriendo queda registrado y cada equipo se despacha ligado a la operación. */
    @Test
    void registrar_equiposDisponibles_guardaElArriendoYDespachaCadaEquipo() {
        var respuesta = service.registrar(EMPRESA_ID, USUARIO_ID, solicitud(linea(1L, "50000"), linea(2L, "70000")));

        ArgumentCaptor<Arriendo> guardado = ArgumentCaptor.forClass(Arriendo.class);
        verify(arriendoRepository).save(guardado.capture());
        Arriendo a = guardado.getValue();
        assertThat(a.getCliente()).isSameAs(cliente);
        assertThat(a.getFecha()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(a.getFechaRetornoComprometida()).isEqualTo(LocalDate.of(2026, 3, 25));
        assertThat(a.getDetalles()).extracting(d -> d.getEquipoId()).containsExactly(1L, 2L);
        verify(equipoService).despacharEnArriendo(EMPRESA_ID, USUARIO_ID, 1L, 500L);
        verify(equipoService).despacharEnArriendo(EMPRESA_ID, USUARIO_ID, 2L, 500L);
        assertThat(respuesta.tarifaDiariaTotal()).isEqualByComparingTo("120000");
    }

    /** CF-02 y PNF-10: el mensaje dice por qué y hasta cuándo el equipo no está disponible. */
    @Test
    void registrar_equipoArrendado_rechazaConLaFechaDeRetorno() {
        when(arriendoRepository.vigentesDelEquipo(EMPRESA_ID, 1L))
                .thenReturn(List.of(new ArriendoVigente(1L, LocalDate.of(2026, 3, 25))));

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrar(EMPRESA_ID, USUARIO_ID, solicitud(linea(1L, "50000"))));

        assertThat(ex.getMessage()).isEqualTo(
                "El equipo SN-1 no está disponible porque se encuentra arrendado hasta el 25 de marzo de 2026.");
        verify(arriendoRepository, never()).save(any());
        verify(equipoService, never()).despacharEnArriendo(anyLong(), anyLong(), anyLong(), anyLong());
    }

    /**
     * CF-03: si la segunda línea falla, la excepción sale del servicio y la transacción revierte
     * todo, incluida la primera línea (la reversión real se prueba contra PostgreSQL).
     */
    @Test
    void registrar_segundaLineaNoDisponible_propagaElErrorParaRevertirTodo() {
        doThrow(new EquipoNoDisponibleException("SN-2", EstadoEquipo.EN_MANTENCION))
                .when(equipoService).despacharEnArriendo(EMPRESA_ID, USUARIO_ID, 2L, 500L);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrar(EMPRESA_ID, USUARIO_ID, solicitud(linea(1L, "50000"), linea(2L, "70000"))));

        assertThat(ex.getMessage()).contains("SN-2").contains("en mantención");
    }

    @Test
    void registrar_retornoNoPosteriorAlInicio_lanzaExcepcion() {
        RegistrarArriendoRequest r = solicitud(linea(1L, "50000"));
        r.setFechaRetornoComprometida(r.getFechaInicio());

        NegocioException ex = assertThrows(NegocioException.class, () -> service.registrar(EMPRESA_ID, USUARIO_ID, r));
        assertThat(ex.getMessage()).contains("posterior a la fecha de inicio");
        verify(arriendoRepository, never()).save(any());
    }

    @Test
    void registrar_sinEquiposSeleccionados_lanzaExcepcion() {
        LineaArriendoRequest noIncluida = linea(1L, "50000");
        noIncluida.setIncluir(false);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrar(EMPRESA_ID, USUARIO_ID, solicitud(noIncluida)));
        assertThat(ex.getMessage()).contains("al menos un equipo");
    }

    @Test
    void registrar_tarifaEnCero_lanzaExcepcion() {
        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrar(EMPRESA_ID, USUARIO_ID, solicitud(linea(1L, "0"))));
        assertThat(ex.getMessage()).contains("tarifa diaria");
    }

    /** PNF-06: un equipo o un cliente de otra empresa se responden como inexistentes. */
    @Test
    void registrar_equipoDeOtraEmpresa_lanzaNoEncontradoYNoGuarda() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.registrar(EMPRESA_ID, USUARIO_ID, solicitud(linea(95L, "50000"))));
        verify(arriendoRepository, never()).save(any());
    }

    @Test
    void registrar_clienteDeOtraEmpresa_lanzaNoEncontrado() {
        RegistrarArriendoRequest r = solicitud(linea(1L, "50000"));
        r.setClienteId(99L);

        assertThrows(RecursoNoEncontradoException.class, () -> service.registrar(EMPRESA_ID, USUARIO_ID, r));
        verify(arriendoRepository, never()).save(any());
    }

    /** HU-15: el cliente devuelve un equipo y conserva el otro; solo esa línea queda cerrada. */
    @Test
    void registrarRetorno_unaLinea_cierraSoloEsaLineaYRecibeElEquipo() {
        Arriendo arriendo = arriendoConDosEquipos();

        var respuesta = service.registrarRetorno(EMPRESA_ID, USUARIO_ID, 500L, retorno(1L, 30L, LocalDate.of(2026, 3, 10)));

        assertThat(arriendo.getDetalles().get(0).getFechaRetornoReal()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(arriendo.getDetalles().get(1).estaPendienteDeRetorno()).isTrue();
        verify(equipoService).recibirDeArriendo(EMPRESA_ID, USUARIO_ID, 1L, 500L, 30L, "Sin daños");
        assertThat(respuesta.lineas()).extracting(l -> l.fechaRetornoReal())
                .containsExactly(LocalDate.of(2026, 3, 10), null);
    }

    @Test
    void registrarRetorno_lineaYaRetornada_lanzaExcepcion() {
        arriendoConDosEquipos();
        service.registrarRetorno(EMPRESA_ID, USUARIO_ID, 500L, retorno(1L, 30L, LocalDate.of(2026, 3, 10)));

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrarRetorno(EMPRESA_ID, USUARIO_ID, 500L, retorno(1L, 30L, LocalDate.of(2026, 3, 10))));
        assertThat(ex.getMessage()).isEqualTo("El equipo SN-1 ya retornó el 10 de marzo de 2026.");
    }

    @Test
    void registrarRetorno_fechaAnteriorAlInicio_lanzaExcepcionSinRecibirElEquipo() {
        arriendoConDosEquipos();

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrarRetorno(EMPRESA_ID, USUARIO_ID, 500L, retorno(1L, 30L, LocalDate.of(2026, 3, 9))));
        assertThat(ex.getMessage()).contains("anterior al inicio");
        verify(equipoService, never()).recibirDeArriendo(anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void registrarRetorno_fechaFutura_lanzaExcepcion() {
        arriendoConDosEquipos();

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrarRetorno(EMPRESA_ID, USUARIO_ID, 500L, retorno(1L, 30L, LocalDate.of(2026, 3, 11))));
        assertThat(ex.getMessage()).contains("futura");
    }

    @Test
    void registrarRetorno_equipoQueNoEstaEnElArriendo_lanzaNoEncontrado() {
        arriendoConDosEquipos();

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.registrarRetorno(EMPRESA_ID, USUARIO_ID, 500L, retorno(3L, 30L, LocalDate.of(2026, 3, 10))));
    }

    /** HU-16 y CF-10: vencidos primero, con los días de atraso; los próximos, con los días que faltan. */
    @Test
    void alertasDeRetorno_vencidoVenceHoyYProximo_calculaLosDiasDeCadaUno() {
        when(arriendoRepository.porRetornarHasta(EMPRESA_ID, LocalDate.of(2026, 3, 13))).thenReturn(List.of(
                new LineaPorRetornar(41L, "Minera Norte SpA", 1L, LocalDate.of(2026, 3, 5)),
                new LineaPorRetornar(42L, "Constructora Andes SpA", 2L, LocalDate.of(2026, 3, 10)),
                new LineaPorRetornar(43L, "Constructora Andes SpA", 3L, LocalDate.of(2026, 3, 13))));

        List<AlertaRetornoResponse> alertas = service.alertasDeRetorno(EMPRESA_ID);

        assertThat(alertas).extracting(AlertaRetornoResponse::dias).containsExactly(-5L, 0L, 3L);
        AlertaRetornoResponse vencida = alertas.get(0);
        assertThat(vencida.vencido()).isTrue();
        assertThat(vencida.diasAtraso()).isEqualTo(5);
        assertThat(vencida.cliente()).isEqualTo("Minera Norte SpA");
        assertThat(vencida.numeroSerie()).isEqualTo("SN-1");
        assertThat(alertas.get(1).vencido()).as("vence hoy: todavía no está vencido").isFalse();
        assertThat(alertas.get(2).diasAtraso()).isZero();
    }

    @Test
    void alertasDeRetorno_sinLineasPorRetornar_quedaVacio() {
        assertThat(service.alertasDeRetorno(EMPRESA_ID)).isEmpty();
    }

    private Arriendo arriendoConDosEquipos() {
        Arriendo arriendo = new Arriendo(mock(Empresa.class), cliente, USUARIO_ID, LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 25), null, null);
        arriendo.agregarLinea(1L, new BigDecimal("50000"));
        arriendo.agregarLinea(2L, new BigDecimal("70000"));
        ReflectionTestUtils.setField(arriendo, "id", 500L);
        when(arriendoRepository.buscarFicha(EMPRESA_ID, 500L)).thenReturn(Optional.of(arriendo));
        return arriendo;
    }

    private static RegistrarRetornoRequest retorno(Long equipoId, Long bodegaId, LocalDate fecha) {
        RegistrarRetornoRequest r = new RegistrarRetornoRequest();
        r.setEquipoId(equipoId);
        r.setBodegaId(bodegaId);
        r.setFechaRetorno(fecha);
        r.setObservacion("Sin daños");
        return r;
    }

    private static RegistrarArriendoRequest solicitud(LineaArriendoRequest... lineas) {
        RegistrarArriendoRequest r = new RegistrarArriendoRequest();
        r.setClienteId(CLIENTE_ID);
        r.setFechaInicio(LocalDate.of(2026, 3, 10));
        r.setFechaRetornoComprometida(LocalDate.of(2026, 3, 25));
        r.setLineas(List.of(lineas));
        return r;
    }

    private static LineaArriendoRequest linea(Long equipoId, String tarifa) {
        return new LineaArriendoRequest(equipoId, true, new BigDecimal(tarifa));
    }

    private static EquipoResponse equipo(Long id) {
        return new EquipoResponse(id, "SN-" + id, "GPS RTK", "Leica", "GS18 T", EstadoEquipo.DISPONIBLE,
                "Bodega Norte", null, null, null, null);
    }
}
