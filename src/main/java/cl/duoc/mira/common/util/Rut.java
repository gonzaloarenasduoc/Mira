package cl.duoc.mira.common.util;

import java.util.Locale;
import java.util.Optional;

/**
 * RUT chileno. Acepta el texto como lo escriba el usuario ("76123456-7", "76.123.456-7", "761234567")
 * y, si el dígito verificador es correcto, lo devuelve con un único formato: {@code 76.123.456-7}.
 */
public final class Rut {

    private Rut() {
    }

    /** RUT normalizado, o vacío si el texto no es un RUT válido. */
    public static Optional<String> normalizar(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        String limpio = texto.replaceAll("[.\\s-]", "").toUpperCase(Locale.ROOT);
        if (!limpio.matches("\\d{7,8}[0-9K]")) {
            return Optional.empty();
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        if (digitoVerificador(cuerpo) != dv) {
            return Optional.empty();
        }
        return Optional.of(conPuntos(cuerpo) + "-" + dv);
    }

    /** Módulo 11 con la serie 2, 3, 4, 5, 6, 7. */
    static char digitoVerificador(String cuerpo) {
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - suma % 11;
        return resto == 11 ? '0' : resto == 10 ? 'K' : Character.forDigit(resto, 10);
    }

    private static String conPuntos(String cuerpo) {
        StringBuilder sb = new StringBuilder(cuerpo);
        for (int i = sb.length() - 3; i > 0; i -= 3) {
            sb.insert(i, '.');
        }
        return sb.toString();
    }
}
