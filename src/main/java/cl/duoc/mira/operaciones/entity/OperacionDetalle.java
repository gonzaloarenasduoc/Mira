package cl.duoc.mira.operaciones.entity;

import cl.duoc.mira.common.entity.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Línea de una operación: un equipo con su valor. En un arriendo, el valor es la tarifa diaria y
 * {@code fechaRetornoReal} queda vacía mientras el equipo no vuelve. El equipo se guarda por
 * identificador: operaciones no usa las entidades de inventario (02-arquitectura.md).
 */
@Getter
@Entity
@Table(name = "operacion_detalle")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperacionDetalle extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operacion_id", nullable = false)
    private Operacion operacion;

    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "fecha_retorno_real")
    private LocalDate fechaRetornoReal;

    @Column(length = 300)
    private String observacion;
}
