package cl.duoc.mira.operaciones.repository;

import cl.duoc.mira.operaciones.entity.Venta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("""
            select distinct v from Venta v
            join fetch v.cliente
            left join fetch v.detalles
            where v.id = :id and v.empresa.id = :empresaId
            """)
    Optional<Venta> buscarFicha(@Param("empresaId") Long empresaId, @Param("id") Long id);
}
