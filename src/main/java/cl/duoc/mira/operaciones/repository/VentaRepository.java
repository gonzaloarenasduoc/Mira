package cl.duoc.mira.operaciones.repository;

import cl.duoc.mira.operaciones.dto.ClienteDeOperacion;import cl.duoc.mira.operaciones.entity.Venta;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    /** Cliente de cada venta del conjunto, en una sola consulta (HU-22). */
    @Query("""
            select new cl.duoc.mira.operaciones.dto.ClienteDeOperacion(v.id, c.razonSocial)
            from Venta v join v.cliente c
            where v.empresa.id = :empresaId and v.id in :ids
            """)
    List<ClienteDeOperacion> clientes(@Param("empresaId") Long empresaId, @Param("ids") Collection<Long> ids);

    @Query("""
            select distinct v from Venta v
            join fetch v.cliente
            left join fetch v.detalles
            where v.id = :id and v.empresa.id = :empresaId
            """)
    Optional<Venta> buscarFicha(@Param("empresaId") Long empresaId, @Param("id") Long id);

    /** Ventas de un cliente con sus líneas, para su historial (HU-19). */
    @Query("""
            select distinct v from Venta v left join fetch v.detalles
            where v.empresa.id = :empresaId and v.cliente.id = :clienteId
            """)
    List<Venta> delCliente(@Param("empresaId") Long empresaId, @Param("clienteId") Long clienteId);

    boolean existsByIdAndEmpresaId(Long id, Long empresaId);

    /** Ventas en que participó un equipo (HU-25). */
    @Query("select distinct v.id from Venta v join v.detalles d where v.empresa.id = :empresaId and d.equipoId = :equipoId")
    List<Long> idsConEquipo(@Param("empresaId") Long empresaId, @Param("equipoId") Long equipoId);
}
