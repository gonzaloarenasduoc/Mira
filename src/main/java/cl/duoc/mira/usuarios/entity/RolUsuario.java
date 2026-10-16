package cl.duoc.mira.usuarios.entity;

/** Los cuatro roles del sistema (D09). La matriz de permisos está en docs/01-contexto-negocio.md. */
public enum RolUsuario {
    ADMINISTRADOR("Administrador"),
    ENCARGADO_BODEGA("Encargado de bodega"),
    VENDEDOR("Vendedor"),
    JEFATURA("Jefatura");

    private final String etiqueta;

    RolUsuario(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
