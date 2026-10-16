package cl.duoc.mira.inventario.repository;

import cl.duoc.mira.inventario.entity.Bodega;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodegaRepository extends JpaRepository<Bodega, Long> {

    List<Bodega> findByEmpresaIdOrderByNombre(Long empresaId);

    List<Bodega> findByEmpresaIdAndActivaTrueOrderByNombre(Long empresaId);

    Optional<Bodega> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<Bodega> findByEmpresaIdAndNombreIgnoreCase(Long empresaId, String nombre);
}
