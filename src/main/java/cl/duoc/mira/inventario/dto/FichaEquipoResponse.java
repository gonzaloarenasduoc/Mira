package cl.duoc.mira.inventario.dto;

import java.util.List;

/**
 * Ficha del equipo con su historial, del movimiento más reciente al más antiguo (HU-07). Si el
 * equipo está en tránsito, {@code trasladoHacia} es la bodega a la que se dirige (HU-09).
 */
public record FichaEquipoResponse(EquipoResponse equipo, List<MovimientoResponse> historial,
                                  String trasladoHacia) {
}
