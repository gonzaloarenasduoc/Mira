package cl.duoc.mira.inventario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.mira.common.entity.Empresa;
import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.inventario.dto.FiltroEquipos;
import cl.duoc.mira.inventario.dto.RegistrarEquipoRequest;
import cl.duoc.mira.inventario.entity.Bodega;
import cl.duoc.mira.inventario.entity.Categoria;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.entity.Equipo;
import cl.duoc.mira.inventario.entity.Movimiento;
import cl.duoc.mira.inventario.entity.TipoMovimiento;
import cl.duoc.mira.inventario.repository.CategoriaRepository;
import cl.duoc.mira.inventario.repository.EquipoRepository;
import cl.duoc.mira.inventario.repository.MovimientoRepository;
import cl.duoc.mira.usuarios.service.UsuarioService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EquipoServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long USUARIO_ID = 5L;
    private static final ZoneId SANTIAGO = ZoneId.of("America/Santiago");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-16T13:00:00Z"), SANTIAGO);

    private EquipoRepository equipoRepository;
    private CategoriaRepository categoriaRepository;
    private MovimientoRepository movimientoRepository;
    private BodegaService bodegaService;
    private EquipoService service;
    private Bodega bodega;

    @BeforeEach
    void preparar() {
        equipoRepository = mock(EquipoRepository.class);
        categoriaRepository = mock(CategoriaRepository.class);
        movimientoRepository = mock(MovimientoRepository.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        bodegaService = mock(BodegaService.class);
        service = new EquipoService(equipoRepository, categoriaRepository, movimientoRepository,
                empresaRepository, bodegaService, mock(UsuarioService.class), RELOJ);

        Empresa empresa = mock(Empresa.class);
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(empresa);
        Categoria categoria = mock(Categoria.class);
        when(categoria.getNombre()).thenReturn("Estación total");
        when(categoriaRepository.findByIdAndEmpresaId(10L, EMPRESA_ID)).thenReturn(Optional.of(categoria));
        bodega = new Bodega(empresa, "Bodega Central", null, "Providencia");
        when(bodegaService.obtener(EMPRESA_ID, 20L)).thenReturn(bodega);
    }

    @Test
    void registrar_numeroSerieRepetido_lanzaExcepcionYNoGuardaNada() {
        when(equipoRepository.existsByEmpresaIdAndNumeroSerieIgnoreCase(EMPRESA_ID, "TS16-001")).thenReturn(true);

        assertThrows(NumeroSerieDuplicadoException.class,
                () -> service.registrar(EMPRESA_ID, USUARIO_ID, solicitud()));
        verify(equipoRepository, never()).save(any());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void registrar_datosValidos_quedaDisponibleYCreaMovimientoDeIngreso() {
        var respuesta = service.registrar(EMPRESA_ID, USUARIO_ID, solicitud());

        ArgumentCaptor<Equipo> equipo = ArgumentCaptor.forClass(Equipo.class);
        ArgumentCaptor<Movimiento> movimiento = ArgumentCaptor.forClass(Movimiento.class);
        verify(equipoRepository).save(equipo.capture());
        verify(movimientoRepository).save(movimiento.capture());

        assertThat(respuesta.estado()).isEqualTo(EstadoEquipo.DISPONIBLE);
        assertThat(equipo.getValue().getEstado()).isEqualTo(EstadoEquipo.DISPONIBLE);
        Movimiento m = movimiento.getValue();
        assertThat(m.getTipo()).isEqualTo(TipoMovimiento.INGRESO);
        assertThat(m.getEquipo()).isSameAs(equipo.getValue());
        assertThat(m.getBodegaDestino()).isSameAs(bodega);
        assertThat(m.getBodegaOrigen()).isNull();
        assertThat(m.getUsuarioId()).isEqualTo(USUARIO_ID);
        assertThat(m.getFecha()).isEqualTo(LocalDateTime.of(2026, 10, 16, 10, 0));
    }

    @Test
    void registrar_bodegaInactiva_lanzaExcepcionYNoGuarda() {
        bodega.cambiarEstado(false);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.registrar(EMPRESA_ID, USUARIO_ID, solicitud()));
        assertThat(ex.getMessage()).contains("Bodega Central").contains("inactiva");
        verify(equipoRepository, never()).save(any());
    }

    @Test
    void ficha_equipoDeOtraEmpresa_lanzaNoEncontrado() {
        when(equipoRepository.buscarFicha(EMPRESA_ID, 99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ficha(EMPRESA_ID, 99L));
    }

    /** PNF-07: el texto del buscador viaja como parámetro, con sus comodines escapados. */
    @Test
    void buscar_textoConCaracteresEspeciales_seEnviaComoPatronEscapado() {
        FiltroEquipos filtro = new FiltroEquipos();
        filtro.setTexto(" ' OR 1=1 -- 100% ");

        assertThat(service.buscar(EMPRESA_ID, filtro)).isEmpty();
        verify(equipoRepository).buscar(EMPRESA_ID, null, null, null, "%' or 1=1 -- 100\\%%");
    }

    private static RegistrarEquipoRequest solicitud() {
        RegistrarEquipoRequest r = new RegistrarEquipoRequest();
        r.setCategoriaId(10L);
        r.setBodegaId(20L);
        r.setNumeroSerie(" TS16-001 ");
        r.setMarca("Leica");
        r.setModelo("TS16");
        return r;
    }
}
