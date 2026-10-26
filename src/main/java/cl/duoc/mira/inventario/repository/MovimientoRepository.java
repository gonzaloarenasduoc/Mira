package cl.duoc.mira.inventario.repository;

import cl.duoc.mira.inventario.entity.Movimiento;
import cl.duoc.mira.inventario.entity.TipoMovimiento;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /** Último movimiento de un tipo para el equipo; para un equipo en tránsito, su salida pendiente. */
    Optional<Movimiento> findFirstByEmpresaIdAndEquipoIdAndTipoOrderByIdDesc(Long empresaId, Long equipoId,
                                                                          TipoMovimiento tipo);

    /** Salidas por traslado de los equipos que siguen en tránsito, es decir, sin llegada registrada. */
    @Query("""
            select m from Movimiento m
            join fetch m.equipo e
            join fetch e.categoria
            join fetch m.bodegaOrigen
            join fetch m.bodegaDestino
            where m.empresa.id = :empresaId
              and m.tipo = cl.duoc.mira.inventario.entity.TipoMovimiento.TRASLADO_SALIDA
              and e.estado = cl.duoc.mira.inventario.entity.EstadoEquipo.EN_TRANSITO
              and m.id = (select max(s.id) from Movimiento s
                          where s.equipo = e
                            and s.tipo = cl.duoc.mira.inventario.entity.TipoMovimiento.TRASLADO_SALIDA)
            order by m.fecha
            """)
    List<Movimiento> trasladosEnTransito(@Param("empresaId") Long empresaId);

    /**
     * Historial de traslados con filtros (HU-10), paginado para no cargarlo completo en memoria.
     * La serie llega como patrón LIKE escapado; el rango de fechas es [desde, hasta).
     */
    @Query(value = """
            select m from Movimiento m
            join fetch m.equipo e
            join fetch e.categoria
            left join fetch m.bodegaOrigen o
            left join fetch m.bodegaDestino d
            where m.empresa.id = :empresaId
              and m.tipo in :tipos
              and lower(e.numeroSerie) like :serie escape '\\'
              and (:bodegaId is null or o.id = :bodegaId or d.id = :bodegaId)
              and m.fecha >= :desde and m.fecha < :hasta
            order by m.fecha desc, m.id desc
            """,
            countQuery = """
            select count(m) from Movimiento m
            join m.equipo e
            left join m.bodegaOrigen o
            left join m.bodegaDestino d
            where m.empresa.id = :empresaId
              and m.tipo in :tipos
              and lower(e.numeroSerie) like :serie escape '\\'
              and (:bodegaId is null or o.id = :bodegaId or d.id = :bodegaId)
              and m.fecha >= :desde and m.fecha < :hasta
            """)
    Page<Movimiento> traslados(@Param("empresaId") Long empresaId,
                               @Param("tipos") Collection<TipoMovimiento> tipos,
                               @Param("serie") String serie,
                               @Param("bodegaId") Long bodegaId,
                               @Param("desde") LocalDateTime desde,
                               @Param("hasta") LocalDateTime hasta,
                               Pageable pagina);
}
