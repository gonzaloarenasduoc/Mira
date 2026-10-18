package cl.duoc.mira.inventario.entity;

import cl.duoc.mira.common.entity.Empresa;
import cl.duoc.mira.common.entity.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Registro histórico de un equipo: es de solo inserción. Si un movimiento fue un error, se
 * registra el movimiento inverso. La operación y el usuario se guardan por identificador para
 * no acoplar el inventario con las entidades de otros módulos.
 */
@Getter
@Entity
@Immutable
@Table(name = "movimiento")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movimiento extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_origen_id")
    private Bodega bodegaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_destino_id")
    private Bodega bodegaDestino;

    @Column(name = "operacion_id")
    private Long operacionId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(length = 300)
    private String observacion;

    /** Crea el movimiento y aplica al equipo el estado y, si corresponde, la bodega de destino. */
    public static Movimiento registrar(Equipo equipo, TipoMovimiento tipo, LocalDateTime fecha,
                                       Bodega origen, Bodega destino, Long usuarioId,
                                       String observacion) {
        Movimiento m = new Movimiento();
        m.empresa = equipo.getEmpresa();
        m.equipo = equipo;
        m.tipo = tipo;
        m.fecha = fecha;
        m.bodegaOrigen = origen;
        m.bodegaDestino = destino;
        m.usuarioId = usuarioId;
        m.observacion = observacion;
        equipo.aplicar(tipo, destino);
        return m;
    }
}
