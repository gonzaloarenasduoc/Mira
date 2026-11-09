package cl.duoc.mira.capacitaciones.dto;

import cl.duoc.mira.capacitaciones.entity.EstadoCapacitacion;
import java.time.LocalDateTime;

public record CapacitacionResponse(
        Long id,
        Long operacionId,
        String tecnico,
        LocalDateTime fechaProgramada,
        String lugar,
        EstadoCapacitacion estado,
        String observaciones) {
}
