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
@Table(name = "bodega")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bodega extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String comuna;

    /** Una bodega inactiva no admite nuevos equipos. No se elimina: tiene historial. */
    @Column(nullable = false)
    private boolean activa;

    public Bodega(Empresa empresa, String nombre, String direccion, String comuna) {
        this.empresa = empresa;
        actualizar(nombre, direccion, comuna);
        this.activa = true;
    }

    public void actualizar(String nombre, String direccion, String comuna) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.comuna = comuna;
    }

    public void cambiarEstado(boolean activa) {
        this.activa = activa;
    }
}
