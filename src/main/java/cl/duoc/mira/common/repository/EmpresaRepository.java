package cl.duoc.mira.common.repository;

import cl.duoc.mira.common.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
