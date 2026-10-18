package cl.duoc.mira.inventario.dto;

import java.util.List;

/** Datos para el formulario de traslado: el equipo y las bodegas a las que puede ir. */
public record PreparacionTrasladoResponse(EquipoResponse equipo, List<BodegaResponse> destinos) {
}
