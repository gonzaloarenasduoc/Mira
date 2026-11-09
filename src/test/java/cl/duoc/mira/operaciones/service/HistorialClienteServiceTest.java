package cl.duoc.mira.operaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.mira.common.entity.Empresa;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.EstadoArriendo;
import cl.duoc.mira.operaciones.dto.OperacionClienteResponse;
import cl.duoc.mira.operaciones.entity.Arriendo;
import cl.duoc.mira.operaciones.entity.Cliente;
import cl.duoc.mira.operaciones.entity.Venta;
import cl.duoc.mira.operaciones.repository.ArriendoRepository;
import cl.duoc.mira.operaciones.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** HU-19: historial de operaciones de un cliente. */
class HistorialClienteServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long CLIENTE_ID = 7L;
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-03-20T13:00:00Z"), ZoneId.of("America/Santiago"));

    private ClienteService clienteService;
    private ArriendoRepository arriendoRepository;
    private VentaRepository ventaRepository;
    private HistorialClienteService service;
    private Cliente cliente;

    @BeforeEach
    void preparar() {
        clienteService = mock(ClienteService.class);
        arriendoRepository = mock(ArriendoRepository.class);
        ventaRepository = mock(VentaRepository.class);
        EquipoService equipoService = mock(EquipoService.class);
        service = new HistorialClienteService(clienteService, arriendoRepository, ventaRepository, equipoService, RELOJ);

        cliente = mock(Cliente.class);
        when(clienteService.obtener(EMPRESA_ID, CLIENTE_ID)).thenReturn(cliente);
        when(clienteService.obtener(EMPRESA_ID, 99L)).thenThrow(new RecursoNoEncontradoException("El cliente"));
        when(equipoService.resumenPorId(eq(EMPRESA_ID), anyCollection())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(1);
            return ids.stream().collect(Collectors.toMap(id -> id, HistorialClienteServiceTest::equipo));
        });
    }

    /** Ventas y arriendos juntos, del más reciente al más antiguo, con su tipo, equipos y total. */
    @Test
    void historial_ventasYArriendos_quedanOrdenadosPorFechaDescendente() {
        Arriendo arriendo = arriendo(10L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 11), 1L, "50000");
        Venta venta = venta(11L, LocalDate.of(2026, 3, 15), 2L, "14200000");
        when(arriendoRepository.delCliente(EMPRESA_ID, CLIENTE_ID)).thenReturn(List.of(arriendo));
        when(ventaRepository.delCliente(EMPRESA_ID, CLIENTE_ID)).thenReturn(List.of(venta));

        List<OperacionClienteResponse> historial = service.historial(EMPRESA_ID, CLIENTE_ID, true);

        assertThat(historial).extracting(OperacionClienteResponse::id).containsExactly(11L, 10L);
        OperacionClienteResponse v = historial.get(0);
        assertThat(v.venta()).isTrue();
        assertThat(v.equipos()).containsExactly("SN-2");
        assertThat(v.total()).isEqualByComparingTo("14200000");
        assertThat(v.totalEstimado()).isFalse();
        OperacionClienteResponse a = historial.get(1);
        assertThat(a.venta()).isFalse();
        assertThat(a.fecha()).as("el arriendo usa su fecha de inicio").isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(a.estado()).isEqualTo(EstadoArriendo.VENCIDO);
        assertThat(a.total()).isEqualByComparingTo("950000");   // D18: 19 días hasta hoy × 50.000
        assertThat(a.totalEstimado()).isTrue();
    }

    /** Matriz de permisos: el encargado de bodega no accede a las ventas, así que no se consultan. */
    @Test
    void historial_sinAccesoAVentas_muestraSoloLosArriendos() {
        when(arriendoRepository.delCliente(EMPRESA_ID, CLIENTE_ID))
                .thenReturn(List.of(arriendo(10L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 25), 1L, "50000")));

        List<OperacionClienteResponse> historial = service.historial(EMPRESA_ID, CLIENTE_ID, false);

        assertThat(historial).extracting(OperacionClienteResponse::venta).containsExactly(false);
        verify(ventaRepository, never()).delCliente(EMPRESA_ID, CLIENTE_ID);
    }

    @Test
    void historial_clienteSinOperaciones_quedaVacio() {
        assertThat(service.historial(EMPRESA_ID, CLIENTE_ID, true)).isEmpty();
    }

    /** PNF-06: un cliente de otra empresa se responde como inexistente. */
    @Test
    void historial_clienteDeOtraEmpresa_lanzaNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> service.historial(EMPRESA_ID, 99L, true));
    }

    private Arriendo arriendo(Long id, LocalDate inicio, LocalDate retorno, Long equipoId, String tarifa) {
        Arriendo a = new Arriendo(mock(Empresa.class), cliente, 5L, inicio, inicio, retorno, null, null);
        a.agregarLinea(equipoId, new BigDecimal(tarifa));
        ReflectionTestUtils.setField(a, "id", id);
        return a;
    }

    private Venta venta(Long id, LocalDate fecha, Long equipoId, String valor) {
        Venta v = new Venta(mock(Empresa.class), cliente, 5L, fecha, null, null);
        v.agregarLinea(equipoId, new BigDecimal(valor));
        ReflectionTestUtils.setField(v, "id", id);
        return v;
    }

    private static EquipoResponse equipo(Long id) {
        return new EquipoResponse(id, "SN-" + id, "GPS RTK", "Leica", "GS18 T", EstadoEquipo.DISPONIBLE,
                "Bodega Norte", null, null, null, null);
    }
}
