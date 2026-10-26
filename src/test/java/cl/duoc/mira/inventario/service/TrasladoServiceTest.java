package cl.duoc.mira.inventario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.mira.common.entity.Empresa;
import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.inventario.dto.FiltroTraslados;
import cl.duoc.mira.inventario.dto.RegistrarTrasladoRequest;
import cl.duoc.mira.inventario.entity.Bodega;
import cl.duoc.mira.inventario.entity.Categoria;
import cl.duoc.mira.inventario.entity.Equipo;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.entity.Movimiento;
import cl.duoc.mira.inventario.entity.TipoMovimiento;
import cl.duoc.mira.inventario.repository.EquipoRepository;
import cl.duoc.mira.inventario.repository.MovimientoRepository;
import cl.duoc.mira.usuarios.service.UsuarioService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

/** HU-09 y CF-08: traslado de un equipo disponible desde Bodega Norte a Bodega Sur. */
class TrasladoServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long USUARIO_ID = 5L;
    private static final Long EQUIPO_ID = 30L;
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-20T14:30:00Z"),
            ZoneId.of("America/Santiago"));

    private EquipoRepository equipoRepository;
    private MovimientoRepository movimientoRepository;
    private TrasladoService service;
    private Bodega norte;
    private Bodega sur;
    private Equipo equipo;

    @BeforeEach
    void preparar() {
        equipoRepository = mock(EquipoRepository.class);
        movimientoRepository = mock(MovimientoRepository.class);
        BodegaService bodegaService = mock(BodegaService.class);
        UsuarioService usuarioService = mock(UsuarioService.class);
        service = new TrasladoService(equipoRepository, movimientoRepository, bodegaService,
                usuarioService, RELOJ);

        Empresa empresa = mock(Empresa.class);
        norte = bodega(empresa, 1L, "Bodega Norte");
        sur = bodega(empresa, 2L, "Bodega Sur");
        when(bodegaService.obtener(EMPRESA_ID, 1L)).thenReturn(norte);
        when(bodegaService.obtener(EMPRESA_ID, 2L)).thenReturn(sur);
        when(usuarioService.nombresPorId(any(), anyCollection())).thenReturn(Map.of(USUARIO_ID, "Ana Rojas"));

        Categoria categoria = mock(Categoria.class);
        when(categoria.getNombre()).thenReturn("GPS RTK");
        equipo = new Equipo(empresa, categoria, norte, "GS18-001", "Leica", "GS18 T", null, null, null, null);
        ReflectionTestUtils.setField(equipo, "id", EQUIPO_ID);
        Movimiento.registrar(equipo, TipoMovimiento.INGRESO, LocalDateTime.now(RELOJ), null, norte, USUARIO_ID, null);
        when(equipoRepository.buscarParaActualizar(EMPRESA_ID, EQUIPO_ID)).thenReturn(Optional.of(equipo));
    }

    @Test
    void registrarSalida_equipoDisponible_quedaEnTransitoYRegistraElMovimiento() {
        var respuesta = service.registrarSalida(EMPRESA_ID, USUARIO_ID, solicitud(2L));

        ArgumentCaptor<Movimiento> guardado = ArgumentCaptor.forClass(Movimiento.class);
        verify(movimientoRepository).save(guardado.capture());
        Movimiento salida = guardado.getValue();
        assertThat(salida.getTipo()).isEqualTo(TipoMovimiento.TRASLADO_SALIDA);
        assertThat(salida.getBodegaOrigen()).isSameAs(norte);
        assertThat(salida.getBodegaDestino()).isSameAs(sur);
        assertThat(salida.getUsuarioId()).isEqualTo(USUARIO_ID);
        assertThat(salida.getFecha()).isEqualTo(LocalDateTime.of(2026, 10, 20, 11, 30));
        assertThat(equipo.getEstado()).isEqualTo(EstadoEquipo.EN_TRANSITO);
        assertThat(equipo.getBodega()).as("sigue en el origen hasta la llegada").isSameAs(norte);
        assertThat(respuesta.responsable()).isEqualTo("Ana Rojas");
    }

    /** CF-08: al confirmar la llegada, el equipo queda disponible en la bodega de destino. */
    @Test
    void confirmarLlegada_trasladoPendiente_dejaElEquipoDisponibleEnElDestino() {
        Movimiento salida = Movimiento.registrar(equipo, TipoMovimiento.TRASLADO_SALIDA,
                LocalDateTime.now(RELOJ), norte, sur, USUARIO_ID, null);
        when(movimientoRepository.findFirstByEmpresaIdAndEquipoIdAndTipoOrderByIdDesc(
                EMPRESA_ID, EQUIPO_ID, TipoMovimiento.TRASLADO_SALIDA)).thenReturn(Optional.of(salida));

        var respuesta = service.confirmarLlegada(EMPRESA_ID, USUARIO_ID, EQUIPO_ID);

        ArgumentCaptor<Movimiento> guardado = ArgumentCaptor.forClass(Movimiento.class);
        verify(movimientoRepository).save(guardado.capture());
        assertThat(guardado.getValue().getTipo()).isEqualTo(TipoMovimiento.TRASLADO_LLEGADA);
        assertThat(guardado.getValue().getBodegaOrigen()).isSameAs(norte);
        assertThat(guardado.getValue().getBodegaDestino()).isSameAs(sur);
        assertThat(equipo.getEstado()).isEqualTo(EstadoEquipo.DISPONIBLE);
        assertThat(equipo.getBodega()).isSameAs(sur);
        assertThat(respuesta.bodegaDestino()).isEqualTo("Bodega Sur");
    }

    @Test
    void registrarSalida_equipoArrendado_lanzaExcepcionYNoRegistraNada() {
        Movimiento.registrar(equipo, TipoMovimiento.SALIDA_ARRIENDO, LocalDateTime.now(RELOJ), norte, null, USUARIO_ID, null);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrarSalida(EMPRESA_ID, USUARIO_ID, solicitud(2L)));
        assertThat(ex.getMessage()).contains("GS18-001").contains("arrendado");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void registrarSalida_destinoIgualAlOrigen_lanzaExcepcion() {
        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrarSalida(EMPRESA_ID, USUARIO_ID, solicitud(1L)));
        assertThat(ex.getMessage()).contains("distinta").contains("Bodega Norte");
        assertThat(equipo.getEstado()).isEqualTo(EstadoEquipo.DISPONIBLE);
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void registrarSalida_destinoInactivo_lanzaExcepcion() {
        sur.cambiarEstado(false);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrarSalida(EMPRESA_ID, USUARIO_ID, solicitud(2L)));
        assertThat(ex.getMessage()).contains("Bodega Sur").contains("inactiva");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void confirmarLlegada_equipoQueNoEstaEnTransito_lanzaExcepcion() {
        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.confirmarLlegada(EMPRESA_ID, USUARIO_ID, EQUIPO_ID));
        assertThat(ex.getMessage()).contains("no tiene un traslado pendiente");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void historial_fechaInicioPosteriorAlTermino_lanzaExcepcion() {
        FiltroTraslados filtro = new FiltroTraslados();
        filtro.setDesde(LocalDate.of(2026, 10, 20));
        filtro.setHasta(LocalDate.of(2026, 10, 1));

        assertThrows(NegocioException.class, () -> service.historial(EMPRESA_ID, filtro, 0));
    }

    private static RegistrarTrasladoRequest solicitud(Long destinoId) {
        RegistrarTrasladoRequest r = new RegistrarTrasladoRequest();
        r.setEquipoId(EQUIPO_ID);
        r.setBodegaDestinoId(destinoId);
        return r;
    }

    private static Bodega bodega(Empresa empresa, Long id, String nombre) {
        Bodega b = new Bodega(empresa, nombre, null, null);
        ReflectionTestUtils.setField(b, "id", id);
        return b;
    }
}
