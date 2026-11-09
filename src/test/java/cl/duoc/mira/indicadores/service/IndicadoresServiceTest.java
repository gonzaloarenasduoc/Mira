package cl.duoc.mira.indicadores.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.indicadores.dto.PanelInventario;
import cl.duoc.mira.indicadores.dto.UtilizacionResponse;
import cl.duoc.mira.inventario.dto.ConteoEquipos;
import cl.duoc.mira.inventario.dto.EquipoEnInventario;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.AlertaRetornoResponse;
import cl.duoc.mira.operaciones.dto.PeriodoArrendado;
import cl.duoc.mira.operaciones.service.ArriendoService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** E009: datos del panel de indicadores (HU-28 y HU-26). */
class IndicadoresServiceTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), ZoneId.of("America/Santiago"));
    private static final LocalDate HOY = LocalDate.of(2026, 11, 9);

    private ArriendoService arriendoService;
    private EquipoService equipoService;
    private IndicadoresService service;

    @BeforeEach
    void preparar() {
        arriendoService = mock(ArriendoService.class);
        equipoService = mock(EquipoService.class);
        service = new IndicadoresService(arriendoService, equipoService, RELOJ);
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

    /** D24: cuenta desde el inicio hasta el día anterior al retorno, solo dentro del periodo. */
    @Test
    void diasArrendado_cuentaSoloLosDiasDelTramoDentroDelPeriodo() {
        LocalDate desde = LocalDate.of(2026, 11, 1);
        LocalDate hasta = LocalDate.of(2026, 11, 9);
        assertThat(IndicadoresService.diasArrendado(tramo("2026-11-02", "2026-11-07"), desde, hasta, HOY)).isEqualTo(5);
        assertThat(IndicadoresService.diasArrendado(tramo("2026-10-25", "2026-11-03"), desde, hasta, HOY))
                .as("empezó antes del periodo").isEqualTo(2);
        assertThat(IndicadoresService.diasArrendado(tramo("2026-11-05", null), desde, hasta, HOY))
                .as("sigue fuera: cuenta hasta hoy").isEqualTo(5);
        assertThat(IndicadoresService.diasArrendado(tramo("2026-11-04", "2026-11-04"), desde, hasta, HOY))
                .as("vuelve el mismo día: un día, como el cobro").isEqualTo(1);
        assertThat(IndicadoresService.diasArrendado(tramo("2026-10-01", "2026-10-20"), desde, hasta, HOY)).isZero();
    }

    /** HU-27: días arrendado sobre días disponibles, global, por categoría y por equipo. */
    @Test
    void utilizacion_calculaGlobalPorCategoriaYPorEquipo() {
        LocalDate desde = LocalDate.of(2026, 10, 31);
        when(equipoService.enInventario(1L)).thenReturn(List.of(
                new EquipoEnInventario(1L, "GPS-A", "GPS RTK", "Leica GS18", LocalDate.of(2026, 1, 1)),
                new EquipoEnInventario(2L, "NIV-B", "Nivel", "Leica NA2", LocalDate.of(2026, 11, 5)),
                new EquipoEnInventario(3L, "GPS-C", "GPS RTK", "Trimble R12i", LocalDate.of(2026, 1, 1)),
                new EquipoEnInventario(4L, "TS-D", "Estación total", "Leica TS16", LocalDate.of(2026, 11, 20))));
        when(arriendoService.arrendadosEntre(1L, desde, HOY)).thenReturn(List.of(
                new PeriodoArrendado(1L, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 7)),
                new PeriodoArrendado(3L, LocalDate.of(2026, 11, 5), null)));

        UtilizacionResponse u = service.utilizacion(1L, desde, HOY);

        assertThat(u.diasPeriodo()).isEqualTo(10);
        assertThat(u.global().equipos()).as("el equipo que ingresa después del periodo no cuenta").isEqualTo(3);
        assertThat(u.global().diasArrendado()).isEqualTo(10);
        assertThat(u.global().diasDisponibles()).as("el nivel ingresó el 05-11: 5 días").isEqualTo(25);
        assertThat(u.global().porcentaje()).isEqualTo(40.0);
        assertThat(u.porCategoria()).extracting(UtilizacionResponse.Fila::nombre).containsExactly("GPS RTK", "Nivel");
        assertThat(u.porCategoria()).extracting(UtilizacionResponse.Fila::porcentaje).containsExactly(50.0, 0.0);
        assertThat(u.masUsados()).extracting(UtilizacionResponse.FilaEquipo::numeroSerie).containsExactly("GPS-A", "GPS-C", "NIV-B");
        assertThat(u.menosUsados()).as("no repite los que ya están en la otra tabla").isEmpty();
    }

    @Test
    void utilizacion_sinFechas_usaLosUltimos30Dias() {
        UtilizacionResponse u = service.utilizacion(1L, null, null);

        assertThat(u.desde()).isEqualTo(LocalDate.of(2026, 10, 11));
        assertThat(u.hasta()).isEqualTo(HOY);
        assertThat(u.diasPeriodo()).isEqualTo(30);
    }

    @Test
    void utilizacion_periodoInvertidoOFuturo_seRechaza() {
        assertThrows(NegocioException.class, () -> service.utilizacion(1L, HOY, HOY.minusDays(1)));
        assertThrows(NegocioException.class, () -> service.utilizacion(1L, HOY, HOY.plusDays(1)));
    }

    private static PeriodoArrendado tramo(String inicio, String retorno) {
        return new PeriodoArrendado(1L, LocalDate.parse(inicio), retorno == null ? null : LocalDate.parse(retorno));
    }

    private static AlertaRetornoResponse alerta(Long arriendoId, long dias) {
        return new AlertaRetornoResponse(arriendoId, "Cliente", 30L, "SN-30", "Nivel Leica NA2",
                LocalDate.of(2026, 11, 9).plusDays(dias), dias);
    }
}
