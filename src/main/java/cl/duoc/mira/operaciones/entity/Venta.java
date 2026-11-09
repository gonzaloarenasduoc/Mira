package cl.duoc.mira.operaciones.entity;

import cl.duoc.mira.common.entity.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    public Venta(Empresa empresa, Cliente cliente, Long usuarioId, LocalDate fecha,
                 String numeroOrdenCompra, String observaciones) {
        super(empresa, cliente, usuarioId, fecha, observaciones);
        this.numeroOrdenCompra = numeroOrdenCompra;
    }

    /** Cada línea es un equipo con su valor de venta (HU-18). */
    public void agregarLinea(Long equipoId, BigDecimal valor) {
        agregarDetalle(equipoId, valor);
    }
}
