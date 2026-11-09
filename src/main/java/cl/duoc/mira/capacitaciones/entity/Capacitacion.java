package cl.duoc.mira.capacitaciones.entity;

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

/**
 * Capacitación vinculada a la venta o al arriendo que la origina (HU-20). La operación se guarda por
 * identificador: capacitaciones no usa las entidades de operaciones (02-arquitectura.md).
 */
@Getter
@Entity
@Table(name = "capacitacion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Capacitacion extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "operacion_id", nullable = false)
    private Long operacionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tecnico_id", nullable = false)
    private Tecnico tecnico;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada;

    @Column(name = "fecha_realizada")
    private LocalDateTime fechaRealizada;

    @Column(length = 200)
    private String lugar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCapacitacion estado;

    @Column(columnDefinition = "text")
    private String observaciones;

    public Capacitacion(Empresa empresa, Long operacionId, Tecnico tecnico, LocalDateTime fechaProgramada,
                        String lugar, String observaciones) {
        this.empresa = empresa;
        this.operacionId = operacionId;
        this.tecnico = tecnico;
        this.fechaProgramada = fechaProgramada;
        this.lugar = lugar;
        this.observaciones = observaciones;
        this.estado = EstadoCapacitacion.PROGRAMADA;
    }
}
