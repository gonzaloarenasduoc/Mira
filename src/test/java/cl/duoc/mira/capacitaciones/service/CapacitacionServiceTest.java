package cl.duoc.mira.capacitaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.mira.capacitaciones.dto.AgendarCapacitacionRequest;
import cl.duoc.mira.capacitaciones.dto.CalendarioMes;
import cl.duoc.mira.capacitaciones.dto.MarcarRealizadaRequest;
import cl.duoc.mira.capacitaciones.dto.CapacitacionResponse;
import cl.duoc.mira.capacitaciones.entity.Capacitacion;
import cl.duoc.mira.capacitaciones.entity.EstadoCapacitacion;
import cl.duoc.mira.capacitaciones.entity.Tecnico;
import cl.duoc.mira.capacitaciones.repository.CapacitacionRepository;
import cl.duoc.mira.common.entity.Empresa;
import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.operaciones.service.VentaService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** HU-20: agendar la capacitación de una operación. */
class CapacitacionServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), ZoneId.of("America/Santiago"));

    private CapacitacionRepository capacitacionRepository;
    private TecnicoService tecnicoService;
    private ArriendoService arriendoService;
    private EquipoService equipoService;
    private VentaService ventaService;
    private CapacitacionService service;
    private Tecnico tecnico;

    @BeforeEach
    void preparar() {
        capacitacionRepository = mock(CapacitacionRepository.class);
        tecnicoService = mock(TecnicoService.class);
        arriendoService = mock(ArriendoService.class);
        equipoService = mock(EquipoService.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        ventaService = mock(VentaService.class);
        service = new CapacitacionService(capacitacionRepository, tecnicoService, empresaRepository, arriendoService,
                ventaService, equipoService, RELOJ);
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(mock(Empresa.class));
        when(arriendoService.existe(EMPRESA_ID, 3L)).thenReturn(true);
        tecnico = new Tecnico(mock(Empresa.class));
        tecnico.actualizar("Andrés", "Pérez", null, null);
        when(tecnicoService.obtener(EMPRESA_ID, 7L)).thenReturn(tecnico);
        when(capacitacionRepository.save(any(Capacitacion.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /** La capacitación queda programada y vinculada al arriendo que la origina. */
    @Test
    void agendarParaArriendo_datosValidos_quedaProgramadaYVinculada() {
        CapacitacionResponse c = service.agendarParaArriendo(EMPRESA_ID, 3L, solicitud(LocalDate.of(2026, 11, 12)));

        assertThat(c.operacionId()).isEqualTo(3L);
        assertThat(c.tecnico()).isEqualTo("Andrés Pérez");
        assertThat(c.fechaProgramada()).isEqualTo(LocalDateTime.of(2026, 11, 12, 10, 0));
        assertThat(c.lugar()).isEqualTo("Faena Los Bronces");
        assertThat(c.estado()).isEqualTo(EstadoCapacitacion.PROGRAMADA);
    }

    @Test
    void agendar_hoy_seAcepta() {
        assertThat(service.agendarParaArriendo(EMPRESA_ID, 3L, solicitud(LocalDate.of(2026, 11, 9))).estado())
                .isEqualTo(EstadoCapacitacion.PROGRAMADA);
    }

    @Test
    void agendar_fechaPasada_seRechaza() {
        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.agendarParaArriendo(EMPRESA_ID, 3L, solicitud(LocalDate.of(2026, 11, 8))));
        assertThat(ex.getMessage()).isEqualTo("La capacitación se agenda para hoy o una fecha posterior.");
        verify(capacitacionRepository, never()).save(any());
    }

    @Test
    void agendar_tecnicoInactivo_seRechaza() {
        tecnico.cambiarEstado(false);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.agendarParaArriendo(EMPRESA_ID, 3L, solicitud(LocalDate.of(2026, 11, 12))));
        assertThat(ex.getMessage()).isEqualTo("El técnico Andrés Pérez está inactivo.");
    }

    /** PNF-06: no se agenda sobre un arriendo de otra empresa. */
    @Test
    void agendar_arriendoDeOtraEmpresa_lanzaNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.agendarParaArriendo(EMPRESA_ID, 99L, solicitud(LocalDate.of(2026, 11, 12))));
        verify(capacitacionRepository, never()).save(any());
    }

    /** HU-20: se sugiere cuando la categoría lo requiere y no hay una programada o realizada. */
    @Test
    void sugerirAgendar_segunLaCategoriaYLasCapacitacionesExistentes() {
        when(equipoService.requierenCapacitacion(EMPRESA_ID, List.of(5L))).thenReturn(true);
        when(equipoService.requierenCapacitacion(EMPRESA_ID, List.of(6L))).thenReturn(false);
        assertThat(service.sugerirAgendar(EMPRESA_ID, 3L, List.of(5L))).isTrue();
        assertThat(service.sugerirAgendar(EMPRESA_ID, 3L, List.of(6L))).as("la categoría no lo requiere").isFalse();

        when(capacitacionRepository.findByEmpresaIdAndOperacionIdOrderByFechaProgramada(EMPRESA_ID, 3L))
                .thenReturn(List.of(capacitacion(EstadoCapacitacion.CANCELADA)));
        assertThat(service.sugerirAgendar(EMPRESA_ID, 3L, List.of(5L))).as("solo hay una cancelada").isTrue();

        when(capacitacionRepository.findByEmpresaIdAndOperacionIdOrderByFechaProgramada(EMPRESA_ID, 3L))
                .thenReturn(List.of(capacitacion(EstadoCapacitacion.PROGRAMADA)));
        assertThat(service.sugerirAgendar(EMPRESA_ID, 3L, List.of(5L))).as("ya está agendada").isFalse();
    }

    /** HU-21 (CF-12): si el técnico ya tiene una programada ese día, se advierte antes de confirmar. */
    @Test
    void agendar_tecnicoOcupadoEseDia_advierteSinGuardar() {
        when(capacitacionRepository.delTecnicoEntre(EMPRESA_ID, null, EstadoCapacitacion.PROGRAMADA,
                LocalDateTime.of(2026, 11, 12, 0, 0), LocalDateTime.of(2026, 11, 13, 0, 0)))
                .thenReturn(List.of(capacitacion(EstadoCapacitacion.PROGRAMADA)));

        ConflictoTecnicoException ex = assertThrows(ConflictoTecnicoException.class,
                () -> service.agendarParaArriendo(EMPRESA_ID, 3L, solicitud(LocalDate.of(2026, 11, 12))));
        assertThat(ex.getMessage())
                .isEqualTo("Andrés Pérez ya tiene una capacitación programada el 12-11-2026: a las 10:00 en Faena.");
        verify(capacitacionRepository, never()).save(any());
    }

    /** HU-21: es una advertencia; si el vendedor confirma, se agenda igual. */
    @Test
    void agendar_tecnicoOcupadoYConfirmado_seAgenda() {
        when(capacitacionRepository.delTecnicoEntre(any(), any(), any(), any(), any()))
                .thenReturn(List.of(capacitacion(EstadoCapacitacion.PROGRAMADA)));
        AgendarCapacitacionRequest r = solicitud(LocalDate.of(2026, 11, 12));
        r.setConfirmarConflicto(true);

        assertThat(service.agendarParaArriendo(EMPRESA_ID, 3L, r).estado()).isEqualTo(EstadoCapacitacion.PROGRAMADA);
        verify(capacitacionRepository).save(any(Capacitacion.class));
    }

    @Test
    void avisoConflicto_sinOtrasEseDia_esNulo_yConVariasLasDetalla() {
        LocalDate dia = LocalDate.of(2026, 11, 12);
        assertThat(service.avisoConflicto(EMPRESA_ID, 7L, dia)).isNull();

        Capacitacion tarde = new Capacitacion(mock(Empresa.class), 4L, tecnico, LocalDateTime.of(2026, 11, 12, 15, 30), "Bodega Central", null);
        when(capacitacionRepository.delTecnicoEntre(EMPRESA_ID, 7L, EstadoCapacitacion.PROGRAMADA,
                dia.atStartOfDay(), dia.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(capacitacion(EstadoCapacitacion.PROGRAMADA), tarde));
        assertThat(service.avisoConflicto(EMPRESA_ID, 7L, dia)).isEqualTo("Andrés Pérez ya tiene 2 capacitaciones "
                + "programadas el 12-11-2026: a las 10:00 en Faena; a las 15:30 en Bodega Central.");
    }

    /** HU-22: el calendario arma semanas de lunes a domingo y ubica cada capacitación en su día. */
    @Test
    void calendario_noviembre2026_semanasDeLunesADomingoConClienteYTecnico() {
        Capacitacion delArriendo = capacitacion(EstadoCapacitacion.PROGRAMADA);
        Capacitacion deLaVenta = new Capacitacion(mock(Empresa.class), 4L, tecnico, LocalDateTime.of(2026, 11, 12, 16, 0), "Copiapó", null);
        when(capacitacionRepository.entre(EMPRESA_ID, LocalDateTime.of(2026, 10, 26, 0, 0), LocalDateTime.of(2026, 12, 7, 0, 0)))
                .thenReturn(List.of(delArriendo, deLaVenta));
        when(arriendoService.clientesPorId(any(), any())).thenReturn(Map.of(3L, "Topografía Atacama Ltda."));
        when(ventaService.clientesPorId(any(), any())).thenReturn(Map.of(4L, "Constructora Los Andes SpA"));

        CalendarioMes cal = service.calendario(EMPRESA_ID, YearMonth.of(2026, 11));

        assertThat(cal.etiqueta()).isEqualTo("Noviembre de 2026");
        assertThat(cal.semanas()).hasSize(6).allSatisfy(semana -> assertThat(semana).hasSize(7));
        assertThat(cal.semanas().getFirst().getFirst().fecha()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(cal.semanas().getFirst().getFirst().delMes()).isFalse();
        assertThat(cal.total()).isEqualTo(2);
        CalendarioMes.Dia jueves12 = cal.semanas().get(2).get(3);
        assertThat(jueves12.fecha()).isEqualTo(LocalDate.of(2026, 11, 12));
        assertThat(jueves12.capacitaciones()).extracting(c -> c.cliente() + " · " + c.tipoOperacion() + " · " + c.tecnico())
                .containsExactly("Topografía Atacama Ltda. · Arriendo · Andrés Pérez",
                        "Constructora Los Andes SpA · Venta · Andrés Pérez");
        assertThat(cal.semanas().get(2).get(0).hoy()).as("lunes 9, hoy según el reloj").isTrue();
        assertThat(cal.diasConCapacitaciones()).singleElement().isEqualTo(jueves12);
    }

    /** HU-22: se marca realizada en la fecha y hora indicadas. */
    @Test
    void marcarRealizada_programada_quedaRealizadaConSuFecha() {
        Capacitacion c = capacitacion(EstadoCapacitacion.PROGRAMADA);
        when(capacitacionRepository.findByIdAndEmpresaId(20L, EMPRESA_ID)).thenReturn(Optional.of(c));

        service.marcarRealizada(EMPRESA_ID, 20L, realizada(LocalDate.of(2026, 11, 9), LocalTime.of(11, 0)));

        assertThat(c.getEstado()).isEqualTo(EstadoCapacitacion.REALIZADA);
        assertThat(c.getFechaRealizada()).isEqualTo(LocalDateTime.of(2026, 11, 9, 11, 0));
    }

    @Test
    void marcarRealizada_fechaFutura_seRechaza() {
        Capacitacion c = capacitacion(EstadoCapacitacion.PROGRAMADA);
        when(capacitacionRepository.findByIdAndEmpresaId(20L, EMPRESA_ID)).thenReturn(Optional.of(c));

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.marcarRealizada(EMPRESA_ID, 20L, realizada(LocalDate.of(2026, 11, 9), LocalTime.of(13, 0))));
        assertThat(ex.getMessage()).isEqualTo("La fecha y hora en que se realizó no pueden ser futuras.");
        assertThat(c.getEstado()).isEqualTo(EstadoCapacitacion.PROGRAMADA);
    }

    /** HU-22: realizada y cancelada son estados finales. */
    @Test
    void cancelarYMarcar_sobreUnEstadoFinal_seRechazan() {
        when(capacitacionRepository.findByIdAndEmpresaId(21L, EMPRESA_ID)).thenReturn(Optional.of(capacitacion(EstadoCapacitacion.CANCELADA)));
        when(capacitacionRepository.findByIdAndEmpresaId(22L, EMPRESA_ID)).thenReturn(Optional.of(capacitacion(EstadoCapacitacion.REALIZADA)));

        assertThat(assertThrows(NegocioException.class, () -> service.cancelar(EMPRESA_ID, 21L)).getMessage())
                .isEqualTo("La capacitación ya está cancelada: no admite cambios.");
        assertThat(assertThrows(NegocioException.class,
                () -> service.marcarRealizada(EMPRESA_ID, 21L, realizada(LocalDate.of(2026, 11, 9), LocalTime.of(9, 0)))).getMessage())
                .isEqualTo("La capacitación ya está cancelada: no admite cambios.");
        assertThat(assertThrows(NegocioException.class, () -> service.cancelar(EMPRESA_ID, 22L)).getMessage())
                .isEqualTo("La capacitación ya está realizada: no admite cambios.");
    }

    @Test
    void cancelar_programada_quedaCancelada() {
        Capacitacion c = capacitacion(EstadoCapacitacion.PROGRAMADA);
        when(capacitacionRepository.findByIdAndEmpresaId(20L, EMPRESA_ID)).thenReturn(Optional.of(c));

        service.cancelar(EMPRESA_ID, 20L);

        assertThat(c.getEstado()).isEqualTo(EstadoCapacitacion.CANCELADA);
    }

    /** PNF-06: una capacitación de otra empresa no existe para esta. */
    @Test
    void detalle_deOtraEmpresa_lanzaNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> service.detalle(EMPRESA_ID, 99L));
        assertThrows(RecursoNoEncontradoException.class, () -> service.cancelar(EMPRESA_ID, 99L));
    }

    private static MarcarRealizadaRequest realizada(LocalDate fecha, LocalTime hora) {
        MarcarRealizadaRequest r = new MarcarRealizadaRequest();
        r.setFecha(fecha);
        r.setHora(hora);
        return r;
    }

    private Capacitacion capacitacion(EstadoCapacitacion estado) {
        Capacitacion c = new Capacitacion(mock(Empresa.class), 3L, tecnico, LocalDateTime.of(2026, 11, 12, 10, 0), "Faena", null);
        ReflectionTestUtils.setField(c, "estado", estado);
        return c;
    }

    private static AgendarCapacitacionRequest solicitud(LocalDate fecha) {
        AgendarCapacitacionRequest r = new AgendarCapacitacionRequest();
        r.setTecnicoId(7L);
        r.setFecha(fecha);
        r.setHora(LocalTime.of(10, 0));
        r.setLugar(" Faena Los Bronces ");
        return r;
    }
}
