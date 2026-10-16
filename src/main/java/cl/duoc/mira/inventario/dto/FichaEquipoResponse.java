package cl.duoc.mira.inventario.dto;

import java.util.List;

/** Ficha del equipo con su historial, del movimiento más reciente al más antiguo (HU-07). */
public record FichaEquipoResponse(EquipoResponse equipo, List<MovimientoResponse> historial) {
}
