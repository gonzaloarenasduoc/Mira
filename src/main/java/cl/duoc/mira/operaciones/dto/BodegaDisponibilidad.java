package cl.duoc.mira.operaciones.dto;

import java.util.List;

/** Unidades de una bodega que coinciden con la consulta, con cuántas están disponibles (HU-12). */
public record BodegaDisponibilidad(String bodega, long disponibles, List<UnidadDisponibilidad> unidades) {

    public int total() {
        return unidades.size();
    }
}
