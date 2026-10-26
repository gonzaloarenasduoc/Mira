package cl.duoc.mira.operaciones.repository;

import cl.duoc.mira.operaciones.entity.Arriendo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArriendoRepository extends JpaRepository<Arriendo, Long> {

    /**
     * Líneas de arriendo sin retorno registrado, con su fecha comprometida, en una sola consulta
     * (HU-13: sin N+1). Es el dato que hace calculada, y no almacenada, la disponibilidad.
     */
    @Query("""
            select new cl.duoc.mira.operaciones.repository.ArriendoVigente(d.equipoId, a.fechaRetornoComprometida)
            from Arriendo a join a.detalles d
            where a.empresa.id = :empresaId and d.fechaRetornoReal is null
            """)
    List<ArriendoVigente> vigentes(@Param("empresaId") Long empresaId);
}
