package cl.duoc.mira.inventario.repository;

import cl.duoc.mira.inventario.entity.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByEmpresaIdAndActivaTrueOrderByNombre(Long empresaId);

    Optional<Categoria> findByIdAndEmpresaId(Long id, Long empresaId);
}
