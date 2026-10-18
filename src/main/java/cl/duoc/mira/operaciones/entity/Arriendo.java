package cl.duoc.mira.operaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entrega temporal de equipos con fecha de retorno comprometida. Su estado (vigente, vencido o
 * cerrado) no se almacena: se calcula con las fechas de retorno de sus líneas (03, 1.6).
 */
@Getter
@Entity
@Table(name = "arriendo")
@DiscriminatorValue("ARRIENDO")
@PrimaryKeyJoinColumn(name = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Arriendo extends Operacion {

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_retorno_comprometida", nullable = false)
    private LocalDate fechaRetornoComprometida;

    @Column(name = "numero_orden_compra", length = 50)
    private String numeroOrdenCompra;
}
