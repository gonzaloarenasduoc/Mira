package cl.duoc.mira.common.util;

import java.util.Locale;

/**
 * Patrones para las búsquedas por texto parcial. El texto del usuario siempre viaja como
 * parámetro de la consulta, nunca concatenado al SQL (PNF-07), y sus comodines se escapan para
 * que un "%" o un "_" se busquen literalmente. Las consultas declaran {@code escape '\'}.
 */
public final class Busqueda {

    private Busqueda() {
    }

    /** Patrón LIKE en minúsculas que contiene el texto; un texto vacío no filtra. */
    public static String contiene(String texto) {
        if (texto == null || texto.isBlank()) {
            return "%";
        }
        String escapado = texto.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escapado + "%";
    }
}
