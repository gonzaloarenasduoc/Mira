package cl.duoc.mira.inventario.entity;

public enum EstadoEquipo {
    DISPONIBLE("Disponible"),
    ARRENDADO("Arrendado"),
    EN_TRANSITO("En tránsito"),
    EN_MANTENCION("En mantención"),
    VENDIDO("Vendido");

    private final String etiqueta;

    EstadoEquipo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
