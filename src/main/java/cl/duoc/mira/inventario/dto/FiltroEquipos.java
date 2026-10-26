package cl.duoc.mira.inventario.dto;

import cl.duoc.mira.inventario.entity.EstadoEquipo;
import lombok.Data;

/** Filtros del listado de equipos (HU-05 y HU-11). Todos son opcionales. */
@Data
public class FiltroEquipos {

    private Long categoriaId;
    private Long bodegaId;
    private EstadoEquipo estado;
    /** Texto parcial que se busca en el número de serie, la marca y el modelo. */
    private String texto;
}
