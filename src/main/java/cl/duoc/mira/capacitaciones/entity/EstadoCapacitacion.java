package cl.duoc.mira.capacitaciones.entity;

public enum EstadoCapacitacion {
    PROGRAMADA("Programada"),
    REALIZADA("Realizada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoCapacitacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
