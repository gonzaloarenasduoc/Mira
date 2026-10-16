package cl.duoc.mira.inventario.entity;

/** Cada tipo de movimiento deja al equipo en un estado definido (docs/03-modelo-datos.md). */
public enum TipoMovimiento {
    INGRESO("Ingreso", EstadoEquipo.DISPONIBLE),
    TRASLADO_SALIDA("Salida por traslado", EstadoEquipo.EN_TRANSITO),
    TRASLADO_LLEGADA("Llegada por traslado", EstadoEquipo.DISPONIBLE),
    SALIDA_ARRIENDO("Salida a arriendo", EstadoEquipo.ARRENDADO),
    RETORNO_ARRIENDO("Retorno de arriendo", EstadoEquipo.DISPONIBLE),
    SALIDA_VENTA("Salida por venta", EstadoEquipo.VENDIDO),
    ENTRADA_MANTENCION("Entrada a mantención", EstadoEquipo.EN_MANTENCION),
    SALIDA_MANTENCION("Salida de mantención", EstadoEquipo.DISPONIBLE);

    private final String etiqueta;
    private final EstadoEquipo estadoResultante;

    TipoMovimiento(String etiqueta, EstadoEquipo estadoResultante) {
        this.etiqueta = etiqueta;
        this.estadoResultante = estadoResultante;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public EstadoEquipo getEstadoResultante() {
        return estadoResultante;
    }
}
