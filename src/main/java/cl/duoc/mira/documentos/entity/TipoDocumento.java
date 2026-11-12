package cl.duoc.mira.documentos.entity;

/** Documentos que se adjuntan a una operación (E008). El sistema los almacena; no los emite. */
public enum TipoDocumento {
    ORDEN_COMPRA("Orden de compra"),
    GUIA_DESPACHO("Guía de despacho"),
    FACTURA("Factura");

    private final String etiqueta;

    TipoDocumento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
