package cl.duoc.mira.operaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Salida definitiva de uno o más equipos. Su total es la suma de los valores de sus líneas. */
@Getter
@Entity
@Table(name = "venta")
@DiscriminatorValue("VENTA")
@PrimaryKeyJoinColumn(name = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Venta extends Operacion {

    @Column(name = "numero_orden_compra", length = 50)
    private String numeroOrdenCompra;
}
