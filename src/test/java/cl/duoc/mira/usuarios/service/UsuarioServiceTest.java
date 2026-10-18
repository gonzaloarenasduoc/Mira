package cl.duoc.mira.usuarios.service;

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
import cl.duoc.mira.usuarios.dto.CrearUsuarioRequest;
import cl.duoc.mira.usuarios.entity.RolUsuario;
import cl.duoc.mira.usuarios.entity.Usuario;
import cl.duoc.mira.usuarios.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UsuarioServiceTest {

    private static final Long EMPRESA_ID = 1L;

    private UsuarioRepository usuarioRepository;
    private EmpresaRepository empresaRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private UsuarioService service;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        empresaRepository = mock(EmpresaRepository.class);
        service = new UsuarioService(usuarioRepository, empresaRepository, encoder);
        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(mock(Empresa.class));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void crear_correoRepetido_lanzaExcepcionYNoGuarda() {
        when(usuarioRepository.existsByEmailIgnoreCase("vendedor@mira.cl")).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class,
                () -> service.crear(EMPRESA_ID, solicitud("Vendedor@Mira.cl")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crear_datosValidos_guardaContrasenaCifradaYCorreoEnMinusculas() {
        service.crear(EMPRESA_ID, solicitud("  Vendedor@Mira.cl "));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        Usuario u = guardado.getValue();
        assertThat(u.getEmail()).isEqualTo("vendedor@mira.cl");
        assertThat(u.getPasswordHash()).isNotEqualTo("clave-segura-1");
        assertThat(encoder.matches("clave-segura-1", u.getPasswordHash())).isTrue();
        assertThat(u.isActivo()).isTrue();
    }

    @Test
    void cambiarEstado_desactivarseASiMismo_lanzaExcepcion() {
        assertThrows(NegocioException.class, () -> service.cambiarEstado(EMPRESA_ID, 7L, false, 7L));
    }

    @Test
    void buscar_usuarioDeOtraEmpresa_lanzaNoEncontrado() {
        when(usuarioRepository.findByIdAndEmpresaId(9L, EMPRESA_ID)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.buscar(EMPRESA_ID, 9L));
    }

    private static CrearUsuarioRequest solicitud(String email) {
        CrearUsuarioRequest r = new CrearUsuarioRequest();
        r.setNombre("Valentina");
        r.setApellido("Soto");
        r.setEmail(email);
        r.setPassword("clave-segura-1");
        r.setRol(RolUsuario.VENDEDOR);
        return r;
    }
}
