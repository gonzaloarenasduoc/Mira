package cl.duoc.mira.inventario.repository;

import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.entity.Equipo;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    boolean existsByEmpresaIdAndNumeroSerieIgnoreCase(Long empresaId, String numeroSerie);

    @Query("""
            select e from Equipo e
            join fetch e.categoria
            join fetch e.bodega
            where e.id = :id and e.empresa.id = :empresaId
            """)
    Optional<Equipo> buscarFicha(@Param("empresaId") Long empresaId, @Param("id") Long id);

    /**
     * Bloquea la fila del equipo hasta el fin de la transacción: dos operaciones simultáneas sobre
     * el mismo equipo se ejecutan una después de la otra y la segunda ve el estado ya cambiado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipo e where e.id = :id and e.empresa.id = :empresaId")
    Optional<Equipo> buscarParaActualizar(@Param("empresaId") Long empresaId, @Param("id") Long id);

    /** Listado con filtros opcionales (HU-05). La serie vacía no filtra. */
    @Query("""
            select e from Equipo e
            join fetch e.categoria c
            join fetch e.bodega b
            where e.empresa.id = :empresaId
              and e.activo = true
              and (:categoriaId is null or c.id = :categoriaId)
              and (:bodegaId is null or b.id = :bodegaId)
              and (:estado is null or e.estado = :estado)
              and lower(e.numeroSerie) like lower(concat('%', :serie, '%'))
            order by c.nombre, e.numeroSerie
            """)
    List<Equipo> buscar(@Param("empresaId") Long empresaId,
                        @Param("categoriaId") Long categoriaId,
                        @Param("bodegaId") Long bodegaId,
                        @Param("estado") EstadoEquipo estado,
                        @Param("serie") String serie);
}
