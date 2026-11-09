package cl.duoc.mira.capacitaciones.repository;

import cl.duoc.mira.capacitaciones.entity.Tecnico;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    List<Tecnico> findByEmpresaIdOrderByApellidoAscNombreAsc(Long empresaId);

    List<Tecnico> findByEmpresaIdAndActivoTrueOrderByApellidoAscNombreAsc(Long empresaId);

    Optional<Tecnico> findByIdAndEmpresaId(Long id, Long empresaId);
}
