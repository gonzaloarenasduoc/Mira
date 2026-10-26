package cl.duoc.mira.operaciones.dto;

import lombok.Data;

/** Criterios de la consulta de disponibilidad (HU-11 y HU-12). Ambos son opcionales. */
@Data
public class FiltroDisponibilidad {

    /** Texto parcial que se busca en el número de serie, la marca y el modelo. */
    private String texto;
    private Long categoriaId;
}
