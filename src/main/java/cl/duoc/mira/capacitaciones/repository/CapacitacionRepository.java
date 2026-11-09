package cl.duoc.mira.capacitaciones.repository;

import cl.duoc.mira.capacitaciones.entity.Capacitacion;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapacitacionRepository extends JpaRepository<Capacitacion, Long> {

    @EntityGraph(attributePaths = "tecnico")
    List<Capacitacion> findByEmpresaIdAndOperacionIdOrderByFechaProgramada(Long empresaId, Long operacionId);
}
