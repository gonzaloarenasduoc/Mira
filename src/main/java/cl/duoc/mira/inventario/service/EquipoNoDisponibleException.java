package cl.duoc.mira.inventario.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import java.util.Locale;
import lombok.Getter;

/** El equipo no está disponible para comprometerse en una operación (CF-02). */
@Getter
public class EquipoNoDisponibleException extends NegocioException {

    private final String numeroSerie;
    private final EstadoEquipo estado;

    public EquipoNoDisponibleException(String numeroSerie, EstadoEquipo estado) {
        super("El equipo " + numeroSerie + " no está disponible porque se encuentra "
                + estado.getEtiqueta().toLowerCase(Locale.ROOT) + ".");
        this.numeroSerie = numeroSerie;
        this.estado = estado;
    }
}
