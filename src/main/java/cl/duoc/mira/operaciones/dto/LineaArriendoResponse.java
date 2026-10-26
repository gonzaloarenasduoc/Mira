package cl.duoc.mira.operaciones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LineaArriendoResponse(
        Long equipoId,
        String numeroSerie,
        String equipo,
        BigDecimal tarifaDiaria,
        LocalDate fechaRetornoReal) {
}
