package cl.duoc.mira.inventario.entity;

import cl.duoc.mira.common.entity.Empresa;
import cl.duoc.mira.common.entity.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "categoria")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Categoria extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(name = "requiere_capacitacion", nullable = false)
    private boolean requiereCapacitacion;

    @Column(nullable = false)
    private boolean activa;
}
