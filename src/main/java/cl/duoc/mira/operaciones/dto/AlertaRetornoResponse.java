package cl.duoc.mira.operaciones.dto;

import java.time.LocalDate;

/**
 * Alerta de retorno de un equipo arrendado (HU-16). {@code dias} es positivo mientras quedan días
 * para la fecha comprometida, cero si vence hoy y negativo si ya venció.
 */
public record AlertaRetornoResponse(
        Long arriendoId,
        String cliente,
        Long equipoId,
        String numeroSerie,
        String equipo,
        LocalDate fechaRetornoComprometida,
        long dias) {

    public boolean vencido() {
        return dias < 0;
    }

    public long diasAtraso() {
        return Math.max(0, -dias);
    }
}
