package cl.duoc.mira.operaciones.service;

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
import cl.duoc.mira.operaciones.dto.GuardarClienteRequest;
import cl.duoc.mira.operaciones.entity.Cliente;
import cl.duoc.mira.operaciones.repository.ClienteRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** HU-29: administración de los clientes de la empresa (D17). */
class ClienteServiceTest {

    private static final Long EMPRESA_ID = 1L;

    private ClienteRepository clienteRepository;
    private ClienteService service;

    @BeforeEach
    void preparar() {
        clienteRepository = mock(ClienteRepository.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(mock(Empresa.class));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new ClienteService(clienteRepository, empresaRepository);
    }

    @Test
    void crear_rutValidoEscritoSinPuntos_loGuardaNormalizadoYActivo() {
        var respuesta = service.crear(EMPRESA_ID, solicitud("12345678-5"));

        ArgumentCaptor<Cliente> guardado = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(guardado.capture());
        assertThat(guardado.getValue().getRut()).isEqualTo("12.345.678-5");
        assertThat(guardado.getValue().getRazonSocial()).isEqualTo("Minera Los Andes SpA");
        assertThat(guardado.getValue().getGiro()).isNull();
        assertThat(respuesta.activo()).isTrue();
    }

    @Test
    void crear_rutConDigitoVerificadorIncorrecto_lanzaExcepcionYNoGuarda() {
        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.crear(EMPRESA_ID, solicitud("12.345.678-9")));
        assertThat(ex.getMessage()).contains("RUT ingresado no es válido");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crear_rutRepetidoEnLaEmpresa_lanzaExcepcionConElClienteExistente() {
        Cliente existente = mock(Cliente.class);
        when(existente.getRazonSocial()).thenReturn("Constructora Sur Ltda.");
        when(clienteRepository.findByEmpresaIdAndRut(EMPRESA_ID, "12.345.678-5")).thenReturn(Optional.of(existente));

        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.crear(EMPRESA_ID, solicitud("123456785")));
        assertThat(ex.getMessage()).isEqualTo("El RUT 12.345.678-5 ya está registrado para Constructora Sur Ltda.");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_desactivar_dejaElClienteInactivo() {
        Cliente cliente = new Cliente(mock(Empresa.class), "12.345.678-5");
        when(clienteRepository.findByIdAndEmpresaId(9L, EMPRESA_ID)).thenReturn(Optional.of(cliente));

        service.cambiarEstado(EMPRESA_ID, 9L, false);

        assertThat(cliente.isActivo()).isFalse();
    }

    /** PNF-06: un cliente de otra empresa se responde como inexistente. */
    @Test
    void actualizar_clienteDeOtraEmpresa_lanzaNoEncontrado() {
        when(clienteRepository.findByIdAndEmpresaId(9L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.actualizar(EMPRESA_ID, 9L, solicitud(null)));
    }

    private static GuardarClienteRequest solicitud(String rut) {
        GuardarClienteRequest r = new GuardarClienteRequest();
        r.setRut(rut);
        r.setRazonSocial(" Minera Los Andes SpA ");
        r.setGiro(" ");
        return r;
    }
}
