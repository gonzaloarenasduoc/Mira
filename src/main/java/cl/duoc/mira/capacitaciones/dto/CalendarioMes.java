package cl.duoc.mira.capacitaciones.dto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Mes del calendario de capacitaciones (HU-22): semanas de lunes a domingo. */
public record CalendarioMes(YearMonth mes, List<List<Dia>> semanas, int total) {

    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.forLanguageTag("es-CL"));

    /** Un día de la grilla; los de los meses vecinos completan la primera y la última semana. */
    public record Dia(LocalDate fecha, boolean delMes, boolean hoy, List<DetalleCapacitacionResponse> capacitaciones) {
    }

    /** Por ejemplo, "Noviembre de 2026". */
    public String etiqueta() {
        String texto = MES.format(mes);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    public YearMonth anterior() {
        return mes.minusMonths(1);
    }

    public YearMonth siguiente() {
        return mes.plusMonths(1);
    }

    /** Días del mes con capacitaciones, para la vista en lista de las pantallas angostas. */
    public List<Dia> diasConCapacitaciones() {
        return semanas.stream().flatMap(List::stream)
                .filter(d -> d.delMes() && !d.capacitaciones().isEmpty()).toList();
    }
}
