package cl.duoc.mira.inventario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.inventario.dto.GuardarBodegaRequest;
import cl.duoc.mira.inventario.entity.Bodega;
import cl.duoc.mira.inventario.repository.BodegaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BodegaServiceTest {

    private static final Long EMPRESA_ID = 1L;

    private BodegaRepository bodegaRepository;
    private BodegaService service;

    @BeforeEach
    void preparar() {
        bodegaRepository = mock(BodegaRepository.class);
        service = new BodegaService(bodegaRepository, mock(EmpresaRepository.class));
    }

    @Test
    void crear_nombreRepetidoSinImportarMayusculas_lanzaExcepcionYNoGuarda() {
        Bodega existente = mock(Bodega.class);
        when(existente.getId()).thenReturn(3L);
        when(existente.getNombre()).thenReturn("Bodega Central");
        when(bodegaRepository.findByEmpresaIdAndNombreIgnoreCase(EMPRESA_ID, "bodega central"))
                .thenReturn(Optional.of(existente));

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.crear(EMPRESA_ID, new GuardarBodegaRequest(" bodega central ", null, null)));
        assertThat(ex.getMessage()).contains("Bodega Central");
        verify(bodegaRepository, never()).save(any());
    }

    @Test
    void actualizar_conservandoSuPropioNombre_seGuarda() {
        Bodega propia = mock(Bodega.class);
        when(propia.getId()).thenReturn(3L);
        when(bodegaRepository.findByIdAndEmpresaId(3L, EMPRESA_ID)).thenReturn(Optional.of(propia));
        when(bodegaRepository.findByEmpresaIdAndNombreIgnoreCase(EMPRESA_ID, "Bodega Central"))
                .thenReturn(Optional.of(propia));

        assertDoesNotThrow(() -> service.actualizar(EMPRESA_ID, 3L,
                new GuardarBodegaRequest("Bodega Central", "Nueva dirección 100", "Providencia")));
        verify(propia).actualizar("Bodega Central", "Nueva dirección 100", "Providencia");
    }
}
