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
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Unidad física individual, identificada por su número de serie. Su estado nunca se cambia sin
 * registrar el movimiento correspondiente en la misma transacción (03-modelo-datos.md, 1.3).
 */
@Getter
@Entity
@Table(name = "equipo")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Equipo extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Bodega de asignación: se mantiene aunque el equipo esté arrendado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @Column(name = "numero_serie", nullable = false, length = 80)
    private String numeroSerie;

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEquipo estado;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Column(name = "valor_adquisicion", precision = 12, scale = 2)
    private BigDecimal valorAdquisicion;

    @Column(name = "tarifa_diaria", precision = 12, scale = 2)
    private BigDecimal tarifaDiaria;

    @Column(columnDefinition = "text")
    private String observaciones;

    /** Baja lógica: un equipo nunca se elimina físicamente. */
    @Column(nullable = false)
    private boolean activo;

    public Equipo(Empresa empresa, Categoria categoria, Bodega bodega, String numeroSerie,
                  String marca, String modelo, LocalDate fechaAdquisicion,
                  BigDecimal valorAdquisicion, BigDecimal tarifaDiaria, String observaciones) {
        this.empresa = empresa;
        this.categoria = categoria;
        this.bodega = bodega;
        this.numeroSerie = numeroSerie;
        this.marca = marca;
        this.modelo = modelo;
        this.fechaAdquisicion = fechaAdquisicion;
        this.valorAdquisicion = valorAdquisicion;
        this.tarifaDiaria = tarifaDiaria;
        this.observaciones = observaciones;
        this.activo = true;
    }

    /** Solo lo invoca EquipoService al registrar un movimiento, nunca por separado. */
    void aplicar(TipoMovimiento tipo) {
        this.estado = tipo.getEstadoResultante();
    }
}
