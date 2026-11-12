package cl.duoc.mira.inventario.dto;

import cl.duoc.mira.inventario.entity.EstadoEquipo;

/** Cantidad de equipos activos de una bodega en un estado (HU-26). */
public record ConteoEquipos(String bodega, EstadoEquipo estado, long cantidad) {
}
