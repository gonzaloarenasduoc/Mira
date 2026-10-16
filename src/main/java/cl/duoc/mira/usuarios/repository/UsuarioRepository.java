package cl.duoc.mira.usuarios.repository;

import cl.duoc.mira.usuarios.entity.Usuario;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Sin filtro de empresa a propósito: el correo es único global y es la credencial de acceso. */
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Usuario> findByEmpresaIdOrderByApellidoAscNombreAsc(Long empresaId);

    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

    List<Usuario> findByEmpresaIdAndIdIn(Long empresaId, Collection<Long> ids);
}
