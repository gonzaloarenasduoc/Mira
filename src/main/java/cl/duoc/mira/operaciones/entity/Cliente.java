package cl.duoc.mira.operaciones.entity;

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

/** Empresa que compra o arrienda equipos. El RUT es único por empresa; la baja es lógica (HU-29). */
@Getter
@Entity
@Table(name = "cliente")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cliente extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 12)
    private String rut;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(length = 150)
    private String giro;

    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String comuna;

    @Column(name = "contacto_nombre", length = 150)
    private String contactoNombre;

    @Column(name = "contacto_email", length = 150)
    private String contactoEmail;

    @Column(name = "contacto_telefono", length = 30)
    private String contactoTelefono;

    @Column(nullable = false)
    private boolean activo;

    public Cliente(Empresa empresa, String rut) {
        this.empresa = empresa;
        this.rut = rut;
        this.activo = true;
    }

    /** El RUT no se edita: identifica al cliente en sus operaciones y documentos. */
    public void actualizar(String razonSocial, String giro, String direccion, String comuna,
                           String contactoNombre, String contactoEmail, String contactoTelefono) {
        this.razonSocial = razonSocial;
        this.giro = giro;
        this.direccion = direccion;
        this.comuna = comuna;
        this.contactoNombre = contactoNombre;
        this.contactoEmail = contactoEmail;
        this.contactoTelefono = contactoTelefono;
    }

    /** Baja lógica: un cliente con operaciones no se elimina. */
    public void cambiarEstado(boolean activo) {
        this.activo = activo;
    }
}
