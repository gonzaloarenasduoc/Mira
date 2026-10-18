package cl.duoc.mira.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Empresa propietaria de los datos. Toda entidad de negocio la referencia (D04). */
@Getter
@Entity
@Table(name = "empresa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Empresa extends EntidadAuditable {

    @Column(nullable = false, length = 12)
    private String rut;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "nombre_fantasia", length = 200)
    private String nombreFantasia;

    @Column(nullable = false)
    private boolean activa;
}
