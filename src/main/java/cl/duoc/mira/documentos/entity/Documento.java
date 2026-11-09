package cl.duoc.mira.documentos.entity;

import cl.duoc.mira.common.entity.EntidadAuditable;
import cl.duoc.mira.common.entity.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Documento adjunto a una operación (E008). El archivo vive en disco, en APP_UPLOAD_DIR; aquí se
 * guarda su referencia (D06). Se adjunta a la operación, no al equipo: si la operación cubre varios
 * equipos, el mismo documento se ve en la ficha de cada uno (CF-13). La operación se guarda por
 * identificador: documentos no usa las entidades de operaciones (02-arquitectura.md).
 */
@Getter
@Entity
@Table(name = "documento")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Documento extends EntidadAuditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "operacion_id", nullable = false)
    private Long operacionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDocumento tipo;

    @Column(name = "numero_folio", length = 50)
    private String numeroFolio;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "tamano_bytes", nullable = false)
    private long tamanoBytes;

    public Documento(Empresa empresa, Long operacionId, TipoDocumento tipo, String numeroFolio, LocalDate fechaEmision,
                     String nombreArchivo, String rutaArchivo, String contentType, long tamanoBytes) {
        this.empresa = empresa;
        this.operacionId = operacionId;
        this.tipo = tipo;
        this.numeroFolio = numeroFolio;
        this.fechaEmision = fechaEmision;
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
        this.contentType = contentType;
        this.tamanoBytes = tamanoBytes;
    }
}
