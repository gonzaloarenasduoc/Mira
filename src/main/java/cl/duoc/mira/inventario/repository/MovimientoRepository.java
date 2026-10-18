package cl.duoc.mira.inventario.repository;

import cl.duoc.mira.inventario.entity.Movimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("""
            select m from Movimiento m
            left join fetch m.bodegaOrigen
            left join fetch m.bodegaDestino
            where m.equipo.id = :equipoId and m.empresa.id = :empresaId
            order by m.fecha desc, m.id desc
            """)
    List<Movimiento> historial(@Param("empresaId") Long empresaId, @Param("equipoId") Long equipoId);
}
