package cl.duoc.mira.indicadores.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cl.duoc.mira.operaciones.dto.AlertaRetornoResponse;
import cl.duoc.mira.operaciones.service.ArriendoService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** HU-28: el panel muestra solo los retornos vencidos, del más atrasado al menos atrasado. */
class IndicadoresServiceTest {

    @Test
    void retornosVencidos_dejaFueraLosQueVencenHoyOMasAdelante() {
        ArriendoService arriendoService = mock(ArriendoService.class);
        when(arriendoService.alertasDeRetorno(1L)).thenReturn(List.of(
                alerta(1L, -10), alerta(2L, -1), alerta(3L, 0), alerta(4L, 2)));

        List<AlertaRetornoResponse> vencidos = new IndicadoresService(arriendoService).retornosVencidos(1L);

        assertThat(vencidos).extracting(AlertaRetornoResponse::arriendoId).containsExactly(1L, 2L);
        assertThat(vencidos).extracting(AlertaRetornoResponse::diasAtraso).containsExactly(10L, 1L);
    }

    private static AlertaRetornoResponse alerta(Long arriendoId, long dias) {
        return new AlertaRetornoResponse(arriendoId, "Cliente", 30L, "SN-30", "Nivel Leica NA2",
                LocalDate.of(2026, 11, 9).plusDays(dias), dias);
    }
}
