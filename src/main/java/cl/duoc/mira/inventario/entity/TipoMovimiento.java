package cl.duoc.mira.inventario.entity;

/**
 * Cada tipo de movimiento deja al equipo en un estado definido (docs/03-modelo-datos.md). Los
 * que reubican asignan el equipo a la bodega de destino del movimiento.
 */
public enum TipoMovimiento {
    INGRESO("Ingreso", EstadoEquipo.DISPONIBLE, true),
    TRASLADO_SALIDA("Salida por traslado", EstadoEquipo.EN_TRANSITO, false),
    TRASLADO_LLEGADA("Llegada por traslado", EstadoEquipo.DISPONIBLE, true),
    SALIDA_ARRIENDO("Salida a arriendo", EstadoEquipo.ARRENDADO, false),
    RETORNO_ARRIENDO("Retorno de arriendo", EstadoEquipo.DISPONIBLE, true),
    SALIDA_VENTA("Salida por venta", EstadoEquipo.VENDIDO, false),
    ENTRADA_MANTENCION("Entrada a mantención", EstadoEquipo.EN_MANTENCION, false),
    SALIDA_MANTENCION("Salida de mantención", EstadoEquipo.DISPONIBLE, false);

    private final String etiqueta;
    private final EstadoEquipo estadoResultante;
    private final boolean reubica;

    TipoMovimiento(String etiqueta, EstadoEquipo estadoResultante, boolean reubica) {
        this.etiqueta = etiqueta;
        this.estadoResultante = estadoResultante;
        this.reubica = reubica;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public EstadoEquipo getEstadoResultante() {
        return estadoResultante;
    }

    public boolean isReubica() {
        return reubica;
    }
}
