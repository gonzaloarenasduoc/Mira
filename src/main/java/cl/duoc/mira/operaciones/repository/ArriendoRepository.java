package cl.duoc.mira.operaciones.repository;

import cl.duoc.mira.operaciones.entity.Arriendo;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    /** Arriendo vigente de un equipo, si lo tiene (regla 1 de 03-modelo-datos.md, sección 5). */
    @Query("""
            select new cl.duoc.mira.operaciones.repository.ArriendoVigente(d.equipoId, a.fechaRetornoComprometida)
            from Arriendo a join a.detalles d
            where a.empresa.id = :empresaId and d.equipoId = :equipoId and d.fechaRetornoReal is null
            """)
    List<ArriendoVigente> vigentesDelEquipo(@Param("empresaId") Long empresaId, @Param("equipoId") Long equipoId);

    /**
     * Líneas sin retorno cuya fecha comprometida es igual o anterior al límite, de la más atrasada a la
     * más próxima (HU-16). Una sola consulta, apoyada en el índice ix_arriendo_fecha_retorno.
     */
    @Query("""
            select new cl.duoc.mira.operaciones.repository.LineaPorRetornar(
                a.id, c.razonSocial, d.equipoId, a.fechaRetornoComprometida)
            from Arriendo a join a.cliente c join a.detalles d
            where a.empresa.id = :empresaId
              and d.fechaRetornoReal is null
              and a.fechaRetornoComprometida <= :limite
            order by a.fechaRetornoComprometida, a.id
            """)
    List<LineaPorRetornar> porRetornarHasta(@Param("empresaId") Long empresaId, @Param("limite") LocalDate limite);

    @Query("""
            select distinct a from Arriendo a
            join fetch a.cliente
            left join fetch a.detalles
            where a.id = :id and a.empresa.id = :empresaId
            """)
    Optional<Arriendo> buscarFicha(@Param("empresaId") Long empresaId, @Param("id") Long id);

    /**
     * Página del listado de arriendos (HU-17), del inicio más reciente al más antiguo, con su cliente.
     * Las líneas se cargan aparte con {@link #conDetalles}: un fetch de colección no se pagina en SQL.
     */
    @EntityGraph(attributePaths = "cliente")
    @Query(value = "select a from Arriendo a where a.empresa.id = :empresaId order by a.fechaInicio desc, a.id desc",
            countQuery = "select count(a) from Arriendo a where a.empresa.id = :empresaId")
    Page<Arriendo> listar(@Param("empresaId") Long empresaId, Pageable pagina);

    /** Arriendos de un cliente con sus líneas, para su historial (HU-19). */
    @Query("""
            select distinct a from Arriendo a left join fetch a.detalles
            where a.empresa.id = :empresaId and a.cliente.id = :clienteId
            """)
    List<Arriendo> delCliente(@Param("empresaId") Long empresaId, @Param("clienteId") Long clienteId);

    /** Líneas de los arriendos de una página, en una sola consulta (sin N+1). */
    @Query("select distinct a from Arriendo a left join fetch a.detalles where a.id in :ids")
    List<Arriendo> conDetalles(@Param("ids") Collection<Long> ids);
}
