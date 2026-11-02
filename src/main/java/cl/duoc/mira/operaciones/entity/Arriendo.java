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

    public Arriendo(Empresa empresa, Cliente cliente, Long usuarioId, LocalDate fecha,
                    LocalDate fechaInicio, LocalDate fechaRetornoComprometida,
                    String numeroOrdenCompra, String observaciones) {
        super(empresa, cliente, usuarioId, fecha, observaciones);
        this.fechaInicio = fechaInicio;
        this.fechaRetornoComprometida = fechaRetornoComprometida;
        this.numeroOrdenCompra = numeroOrdenCompra;
    }

    /**
     * Registra el retorno de una línea (HU-15). Cada equipo retorna por separado: el cliente puede
     * devolver uno y conservar otro. Las reglas de fecha las valida ArriendoService.
     */
    public void registrarRetorno(OperacionDetalle linea, LocalDate fecha, String observacion) {
        linea.registrarRetorno(fecha, observacion);
    }

    /** Cada línea es un equipo con su tarifa diaria (HU-14). */
    public void agregarLinea(Long equipoId, BigDecimal tarifaDiaria) {
        agregarDetalle(equipoId, tarifaDiaria);
    }
}
