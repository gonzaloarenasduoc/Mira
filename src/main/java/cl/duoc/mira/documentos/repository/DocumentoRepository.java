package cl.duoc.mira.documentos.repository;

import cl.duoc.mira.documentos.entity.Documento;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByEmpresaIdAndOperacionIdInOrderByFechaEmisionDescIdDesc(Long empresaId, Collection<Long> operaciones);
}
