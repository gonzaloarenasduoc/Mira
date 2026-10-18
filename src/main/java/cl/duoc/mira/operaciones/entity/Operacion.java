package cl.duoc.mira.operaciones.entity;

import cl.duoc.mira.common.entity.Empresa;
import cl.duoc.mira.common.entity.EntidadAuditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Venta o arriendo de uno o más equipos (03-modelo-datos.md, 1.4 y 1.5). Herencia JOINED: las
 * tablas hijas comparten la clave primaria y la empresa de esta tabla. El usuario se guarda por
 * identificador para no acoplar operaciones con las entidades del módulo de usuarios.
 */
@Getter
@Entity
@Table(name = "operacion")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Operacion extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(columnDefinition = "text")
    private String observaciones;

    @OneToMany(mappedBy = "operacion", cascade = CascadeType.ALL)
    private List<OperacionDetalle> detalles = new ArrayList<>();
}
