package cl.duoc.mira.documentos.repository;

import cl.duoc.mira.documentos.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {
}
