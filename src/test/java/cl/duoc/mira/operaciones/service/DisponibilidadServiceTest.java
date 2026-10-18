package cl.duoc.mira.operaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.BodegaDisponibilidad;
import cl.duoc.mira.operaciones.dto.DisponibilidadResponse;
import cl.duoc.mira.operaciones.dto.FiltroDisponibilidad;
import cl.duoc.mira.operaciones.dto.UnidadDisponibilidad;
import cl.duoc.mira.operaciones.repository.ArriendoRepository;
import cl.duoc.mira.operaciones.repository.ArriendoVigente;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-12, HU-13 y CF-07: disponibilidad consolidada, calculada y no almacenada. */
class DisponibilidadServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-03-20T15:00:00Z"),
            ZoneId.of("America/Santiago"));

    private EquipoService equipoService;
    private ArriendoRepository arriendoRepository;
    private DisponibilidadService service;

    @BeforeEach
    void preparar() {
        equipoService = mock(EquipoService.class);
        arriendoRepository = mock(ArriendoRepository.class);
        service = new DisponibilidadService(equipoService, arriendoRepository, RELOJ);
    }

    /** CF-07: dos GPS RTK disponibles, cada uno con su bodega, y uno arrendado con su fecha de retorno. */
    @Test
    void consultar_tresGpsRtk_muestraDosDisponiblesYUnoArrendadoConSuRetorno() {
        when(equipoService.buscar(eq(EMPRESA_ID), any())).thenReturn(List.of(
                equipo(1L, "GS18-001", EstadoEquipo.DISPONIBLE, "Bodega Norte"),
                equipo(2L, "GS18-002", EstadoEquipo.DISPONIBLE, "Bodega Sur"),
                equipo(3L, "GS18-003", EstadoEquipo.ARRENDADO, "Bodega Norte")));
        when(arriendoRepository.vigentes(EMPRESA_ID))
                .thenReturn(List.of(new ArriendoVigente(3L, LocalDate.of(2026, 3, 25))));

        DisponibilidadResponse r = service.consultar(EMPRESA_ID, filtro("GPS"));

        assertThat(r.disponibles()).isEqualTo(2);
        assertThat(r.unidades()).isEqualTo(3);
        assertThat(r.bodegas()).extracting(BodegaDisponibilidad::bodega).containsExactly("Bodega Norte", "Bodega Sur");
        BodegaDisponibilidad norte = r.bodegas().get(0);
        assertThat(norte.disponibles()).isEqualTo(1);
        UnidadDisponibilidad arrendado = norte.unidades().get(1);
        assertThat(arrendado.numeroSerie()).isEqualTo("GS18-003");
        assertThat(arrendado.disponible()).isFalse();
        assertThat(arrendado.fechaRetorno()).isEqualTo(LocalDate.of(2026, 3, 25));
        assertThat(arrendado.vencido()).isFalse();
    }

    /** Regla 1.6: un equipo con arriendo vigente no cuenta como disponible, aunque su estado lo diga. */
    @Test
    void consultar_equipoDisponibleConArriendoVigente_noCuentaComoDisponible() {
        when(equipoService.buscar(eq(EMPRESA_ID), any()))
                .thenReturn(List.of(equipo(1L, "TS16-001", EstadoEquipo.DISPONIBLE, "Bodega Central")));
        when(arriendoRepository.vigentes(EMPRESA_ID))
                .thenReturn(List.of(new ArriendoVigente(1L, LocalDate.of(2026, 4, 1))));

        DisponibilidadResponse r = service.consultar(EMPRESA_ID, filtro(null));

        assertThat(r.disponibles()).isZero();
        assertThat(r.bodegas().get(0).unidades().get(0).disponible()).isFalse();
    }

    /** HU-13: un arriendo con la fecha comprometida ya pasada se marca vencido, con sus días de atraso. */
    @Test
    void consultar_arriendoVencido_calculaLosDiasDeAtraso() {
        when(equipoService.buscar(eq(EMPRESA_ID), any()))
                .thenReturn(List.of(equipo(3L, "M300-001", EstadoEquipo.ARRENDADO, "Bodega Central")));
        when(arriendoRepository.vigentes(EMPRESA_ID))
                .thenReturn(List.of(new ArriendoVigente(3L, LocalDate.of(2026, 3, 15))));

        UnidadDisponibilidad u = service.consultar(EMPRESA_ID, filtro(null)).bodegas().get(0).unidades().get(0);

        assertThat(u.vencido()).isTrue();
        assertThat(u.diasAtraso()).isEqualTo(5);
    }

    /** CF-09: un equipo vendido deja de aparecer en la consulta de disponibilidad. */
    @Test
    void consultar_equipoVendido_noApareceEnLaConsulta() {
        when(equipoService.buscar(eq(EMPRESA_ID), any())).thenReturn(List.of(
                equipo(1L, "NA2-001", EstadoEquipo.VENDIDO, "Bodega Norte"),
                equipo(2L, "NA2-002", EstadoEquipo.EN_MANTENCION, "Bodega Norte")));

        DisponibilidadResponse r = service.consultar(EMPRESA_ID, filtro(null));

        assertThat(r.unidades()).isEqualTo(1);
        assertThat(r.bodegas().get(0).unidades()).extracting(UnidadDisponibilidad::numeroSerie)
                .containsExactly("NA2-002");
        assertThat(r.disponibles()).isZero();
    }

    private static FiltroDisponibilidad filtro(String texto) {
        FiltroDisponibilidad f = new FiltroDisponibilidad();
        f.setTexto(texto);
        return f;
    }

    private static EquipoResponse equipo(Long id, String serie, EstadoEquipo estado, String bodega) {
        return new EquipoResponse(id, serie, "GPS RTK", "Leica", "GS18 T", estado, bodega,
                null, null, null, null);
    }
}
