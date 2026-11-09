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
import cl.duoc.mira.operaciones.dto.LineaVentaRequest;
import cl.duoc.mira.operaciones.dto.RegistrarVentaRequest;
import cl.duoc.mira.operaciones.entity.Cliente;
import cl.duoc.mira.operaciones.entity.Venta;
import cl.duoc.mira.operaciones.repository.ArriendoRepository;
import cl.duoc.mira.operaciones.repository.ArriendoVigente;
import cl.duoc.mira.operaciones.repository.VentaRepository;
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

/** HU-18: registro de una venta (CF-09). */
class VentaServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long USUARIO_ID = 5L;
    private static final Long CLIENTE_ID = 7L;
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-03-10T13:00:00Z"), ZoneId.of("America/Santiago"));

    private VentaRepository ventaRepository;
    private ArriendoRepository arriendoRepository;
    private EquipoService equipoService;
    private ClienteService clienteService;
    private VentaService service;
    private Cliente cliente;

    @BeforeEach
    void preparar() {
        ventaRepository = mock(VentaRepository.class);
        arriendoRepository = mock(ArriendoRepository.class);
        equipoService = mock(EquipoService.class);
        clienteService = mock(ClienteService.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        service = new VentaService(ventaRepository, arriendoRepository, clienteService, equipoService, empresaRepository, RELOJ);

        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(mock(Empresa.class));
        cliente = mock(Cliente.class);
        when(cliente.isActivo()).thenReturn(true);
        when(cliente.getRazonSocial()).thenReturn("Constructora Andes SpA");
        when(clienteService.obtener(EMPRESA_ID, CLIENTE_ID)).thenReturn(cliente);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta venta = inv.getArgument(0);
            ReflectionTestUtils.setField(venta, "id", 700L);
            return venta;
        });
        when(equipoService.resumenPorId(eq(EMPRESA_ID), anyCollection())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(1);
            return ids.stream().filter(id -> id < 90).collect(Collectors.toMap(id -> id, VentaServiceTest::equipo));
        });
    }

    /** CF-09: la venta queda registrada con su fecha y cada equipo se despacha ligado a la operación. */
    @Test
    void registrar_equiposDisponibles_guardaLaVentaYDespachaCadaEquipo() {
        var respuesta = service.registrar(EMPRESA_ID, USUARIO_ID,
                solicitud(LocalDate.of(2026, 3, 6), linea(1L, "14200000"), linea(2L, "9800000")));

        ArgumentCaptor<Venta> guardada = ArgumentCaptor.forClass(Venta.class);
        verify(ventaRepository).save(guardada.capture());
        Venta v = guardada.getValue();
        assertThat(v.getCliente()).isSameAs(cliente);
        assertThat(v.getFecha()).isEqualTo(LocalDate.of(2026, 3, 6));
        assertThat(v.getDetalles()).extracting(d -> d.getEquipoId()).containsExactly(1L, 2L);
        verify(equipoService).despacharEnVenta(EMPRESA_ID, USUARIO_ID, 1L, 700L);
        verify(equipoService).despacharEnVenta(EMPRESA_ID, USUARIO_ID, 2L, 700L);
        assertThat(respuesta.total()).isEqualByComparingTo("24000000");
    }

    @Test
    void registrar_fechaFutura_lanzaExcepcionSinGuardar() {
        NegocioException ex = assertThrows(NegocioException.class, () -> service.registrar(EMPRESA_ID, USUARIO_ID,
                solicitud(LocalDate.of(2026, 3, 11), linea(1L, "14200000"))));

        assertThat(ex.getMessage()).isEqualTo("La fecha de la venta no puede ser futura.");
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void registrar_sinEquiposSeleccionados_lanzaExcepcion() {
        RegistrarVentaRequest r = solicitud(LocalDate.of(2026, 3, 10), new LineaVentaRequest(1L, false, new BigDecimal("1000")));

        NegocioException ex = assertThrows(NegocioException.class, () -> service.registrar(EMPRESA_ID, USUARIO_ID, r));
        assertThat(ex.getMessage()).isEqualTo("Seleccione al menos un equipo para vender.");
    }

    @Test
    void registrar_valorEnCero_lanzaExcepcion() {
        assertThrows(NegocioException.class, () -> service.registrar(EMPRESA_ID, USUARIO_ID,
                solicitud(LocalDate.of(2026, 3, 10), linea(1L, "0"))));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void registrar_equipoConArriendoVigente_lanzaExcepcionSinGuardar() {
        when(arriendoRepository.vigentesDelEquipo(EMPRESA_ID, 1L))
                .thenReturn(List.of(new ArriendoVigente(1L, LocalDate.of(2026, 3, 25))));

        NegocioException ex = assertThrows(NegocioException.class, () -> service.registrar(EMPRESA_ID, USUARIO_ID,
                solicitud(LocalDate.of(2026, 3, 10), linea(1L, "14200000"))));
        assertThat(ex.getMessage()).isEqualTo("El equipo SN-1 no se puede vender porque tiene un arriendo vigente.");
        verify(ventaRepository, never()).save(any());
    }

    /** Si una línea no se puede despachar, la excepción sale del servicio y revierte la transacción completa. */
    @Test
    void registrar_segundaLineaNoDisponible_propagaElErrorParaRevertirTodo() {
        doThrow(new EquipoNoDisponibleException("SN-2", EstadoEquipo.VENDIDO))
                .when(equipoService).despacharEnVenta(EMPRESA_ID, USUARIO_ID, 2L, 700L);

        NegocioException ex = assertThrows(NegocioException.class, () -> service.registrar(EMPRESA_ID, USUARIO_ID,
                solicitud(LocalDate.of(2026, 3, 10), linea(1L, "14200000"), linea(2L, "9800000"))));
        assertThat(ex.getMessage()).isEqualTo("El equipo SN-2 no está disponible porque se encuentra vendido.");
    }

    @Test
    void registrar_equipoDeOtraEmpresa_lanzaNoEncontradoYNoGuarda() {
        assertThrows(RecursoNoEncontradoException.class, () -> service.registrar(EMPRESA_ID, USUARIO_ID,
                solicitud(LocalDate.of(2026, 3, 10), linea(95L, "14200000"))));
        verify(ventaRepository, never()).save(any());
        verify(equipoService, never()).despacharEnVenta(anyLong(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void registrar_clienteInactivo_lanzaExcepcion() {
        when(cliente.isActivo()).thenReturn(false);

        NegocioException ex = assertThrows(NegocioException.class, () -> service.registrar(EMPRESA_ID, USUARIO_ID,
                solicitud(LocalDate.of(2026, 3, 10), linea(1L, "14200000"))));
        assertThat(ex.getMessage()).isEqualTo("El cliente Constructora Andes SpA está inactivo.");
    }

    /** PNF-06: una venta de otra empresa se responde como inexistente. */
    @Test
    void ficha_ventaDeOtraEmpresa_lanzaNoEncontrado() {
        when(ventaRepository.buscarFicha(EMPRESA_ID, 800L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ficha(EMPRESA_ID, 800L));
    }

    private static RegistrarVentaRequest solicitud(LocalDate fecha, LineaVentaRequest... lineas) {
        RegistrarVentaRequest r = new RegistrarVentaRequest();
        r.setClienteId(CLIENTE_ID);
        r.setFecha(fecha);
        r.setLineas(List.of(lineas));
        return r;
    }

    private static LineaVentaRequest linea(Long equipoId, String valor) {
        return new LineaVentaRequest(equipoId, true, new BigDecimal(valor));
    }

    private static EquipoResponse equipo(Long id) {
        return new EquipoResponse(id, "SN-" + id, "GPS RTK", "Leica", "GS18 T", EstadoEquipo.DISPONIBLE,
                "Bodega Norte", null, null, null, null);
    }
}
