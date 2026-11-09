package cl.duoc.mira.capacitaciones.repository;

import cl.duoc.mira.capacitaciones.entity.Capacitacion;
import cl.duoc.mira.capacitaciones.entity.EstadoCapacitacion;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CapacitacionRepository extends JpaRepository<Capacitacion, Long> {

    @EntityGraph(attributePaths = "tecnico")
    List<Capacitacion> findByEmpresaIdAndOperacionIdOrderByFechaProgramada(Long empresaId, Long operacionId);

    /** HU-21: capacitaciones de un técnico en un estado, entre dos instantes (el segundo, excluido). */
    @Query("""
            SELECT c FROM Capacitacion c JOIN FETCH c.tecnico t
            WHERE c.empresa.id = :empresaId AND t.id = :tecnicoId AND c.estado = :estado
              AND c.fechaProgramada >= :desde AND c.fechaProgramada < :hasta
            ORDER BY c.fechaProgramada""")
    List<Capacitacion> delTecnicoEntre(@Param("empresaId") Long empresaId, @Param("tecnicoId") Long tecnicoId,
                                       @Param("estado") EstadoCapacitacion estado,
                                       @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
