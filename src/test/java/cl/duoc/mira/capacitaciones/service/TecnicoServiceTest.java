package cl.duoc.mira.capacitaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cl.duoc.mira.capacitaciones.dto.GuardarTecnicoRequest;
import cl.duoc.mira.capacitaciones.dto.TecnicoResponse;
import cl.duoc.mira.capacitaciones.entity.Tecnico;
import cl.duoc.mira.capacitaciones.repository.TecnicoRepository;
import cl.duoc.mira.common.entity.Empresa;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-30: administrar los técnicos que dictan las capacitaciones (D25). */
class TecnicoServiceTest {

    private TecnicoRepository tecnicoRepository;
    private TecnicoService service;

    @BeforeEach
    void preparar() {
        tecnicoRepository = mock(TecnicoRepository.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        service = new TecnicoService(tecnicoRepository, empresaRepository);
        when(empresaRepository.getReferenceById(1L)).thenReturn(mock(Empresa.class));
        when(tecnicoRepository.save(any(Tecnico.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crear_guardaElTecnicoActivoConLosOpcionalesVaciosComoNulos() {
        TecnicoResponse t = service.crear(1L, new GuardarTecnicoRequest(" Andrés ", " Pérez ", " ", ""));

        assertThat(t.nombreCompleto()).isEqualTo("Andrés Pérez");
        assertThat(t.email()).isNull();
        assertThat(t.telefono()).isNull();
        assertThat(t.activo()).isTrue();
    }

    /** No se elimina: se desactiva, porque puede tener capacitaciones asociadas. */
    @Test
    void cambiarEstado_desactivaElTecnico() {
        Tecnico tecnico = new Tecnico(mock(Empresa.class));
        when(tecnicoRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(tecnico));

        service.cambiarEstado(1L, 5L, false);

        assertThat(tecnico.isActivo()).isFalse();
    }

    /** PNF-06: un técnico de otra empresa se responde como inexistente. */
    @Test
    void buscar_tecnicoDeOtraEmpresa_lanzaNoEncontrado() {
        when(tecnicoRepository.findByIdAndEmpresaId(9L, 1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.buscar(1L, 9L));
    }
}
