package cl.duoc.mira.indicadores.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cl.duoc.mira.indicadores.dto.PanelInventario;
import cl.duoc.mira.inventario.dto.ConteoEquipos;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.AlertaRetornoResponse;
import cl.duoc.mira.operaciones.service.ArriendoService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** E009: datos del panel de indicadores (HU-28 y HU-26). */
class IndicadoresServiceTest {

    private ArriendoService arriendoService;
    private EquipoService equipoService;
    private IndicadoresService service;

    @BeforeEach
    void preparar() {
        arriendoService = mock(ArriendoService.class);
        equipoService = mock(EquipoService.class);
        service = new IndicadoresService(arriendoService, equipoService);
    }

    /** HU-28: solo los retornos vencidos, del más atrasado al menos atrasado. */
    @Test
    void retornosVencidos_dejaFueraLosQueVencenHoyOMasAdelante() {
        when(arriendoService.alertasDeRetorno(1L)).thenReturn(List.of(
                alerta(1L, -10), alerta(2L, -1), alerta(3L, 0), alerta(4L, 2)));

        List<AlertaRetornoResponse> vencidos = service.retornosVencidos(1L);

        assertThat(vencidos).extracting(AlertaRetornoResponse::arriendoId).containsExactly(1L, 2L);
        assertThat(vencidos).extracting(AlertaRetornoResponse::diasAtraso).containsExactly(10L, 1L);
    }

    /** HU-26: una serie por estado con la cantidad de cada bodega; los vendidos se informan aparte (D23). */
    @Test
    void inventario_armaUnaSeriePorEstadoYDejaLosVendidosAparte() {
        when(equipoService.conteoPorBodegaYEstado(1L)).thenReturn(List.of(
                new ConteoEquipos("Bodega Central", EstadoEquipo.DISPONIBLE, 3),
                new ConteoEquipos("Bodega Central", EstadoEquipo.ARRENDADO, 1),
                new ConteoEquipos("Bodega Central", EstadoEquipo.VENDIDO, 2),
                new ConteoEquipos("Bodega Norte", EstadoEquipo.ARRENDADO, 2),
                new ConteoEquipos("Bodega Norte", EstadoEquipo.EN_TRANSITO, 1)));

        PanelInventario panel = service.inventario(1L);

        assertThat(panel.bodegas()).containsExactly("Bodega Central", "Bodega Norte");
        assertThat(panel.series()).extracting(PanelInventario.SerieEstado::estado)
                .containsExactly("DISPONIBLE", "ARRENDADO", "EN_TRANSITO", "EN_MANTENCION");
        assertThat(panel.series().get(0).porBodega()).containsExactly(3L, 0L);
        assertThat(panel.series().get(1).porBodega()).containsExactly(1L, 2L);
        assertThat(panel.series().get(1).total()).isEqualTo(3);
        assertThat(panel.totalPorBodega()).containsExactly(4L, 3L);
        assertThat(panel.total()).isEqualTo(7);
        assertThat(panel.vendidos()).isEqualTo(2);
    }

    /** Una bodega cuyos equipos están todos vendidos no aparece en el gráfico. */
    @Test
    void inventario_bodegaSoloConVendidos_noApareceEnElGrafico() {
        when(equipoService.conteoPorBodegaYEstado(1L)).thenReturn(List.of(
                new ConteoEquipos("Bodega Sur", EstadoEquipo.VENDIDO, 4)));

        PanelInventario panel = service.inventario(1L);

        assertThat(panel.bodegas()).isEmpty();
        assertThat(panel.total()).isZero();
        assertThat(panel.vendidos()).isEqualTo(4);
    }

    private static AlertaRetornoResponse alerta(Long arriendoId, long dias) {
        return new AlertaRetornoResponse(arriendoId, "Cliente", 30L, "SN-30", "Nivel Leica NA2",
                LocalDate.of(2026, 11, 9).plusDays(dias), dias);
    }
}
