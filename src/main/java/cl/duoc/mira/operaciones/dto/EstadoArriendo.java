package cl.duoc.mira.operaciones.dto;

/**
 * Estado de un arriendo. No se almacena: se calcula con las fechas de retorno de sus líneas
 * (03-modelo-datos.md, 1.6). Cerrado cuando todos sus equipos retornaron; vencido cuando alguno sigue
 * fuera después de la fecha comprometida; vigente en otro caso.
 */
public enum EstadoArriendo {
    VIGENTE("Vigente"),
    VENCIDO("Vencido"),
    CERRADO("Cerrado");

    private final String etiqueta;

    EstadoArriendo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
