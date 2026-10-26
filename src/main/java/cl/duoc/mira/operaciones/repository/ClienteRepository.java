package cl.duoc.mira.operaciones.repository;

import cl.duoc.mira.operaciones.entity.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByEmpresaIdAndActivoTrueOrderByRazonSocial(Long empresaId);

    Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);
}
