package cl.duoc.mira.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** HU-29: el RUT se valida con su dígito verificador y se guarda con un único formato. */
class RutTest {

    @ParameterizedTest
    @ValueSource(strings = {"12.345.678-5", "12345678-5", "123456785", " 12.345.678 - 5 "})
    void normalizar_distintasFormasDelMismoRut_dejanElMismoFormato(String texto) {
        assertThat(Rut.normalizar(texto)).contains("12.345.678-5");
    }

    @Test
    void normalizar_digitoVerificadorKyCero_seAceptan() {
        assertThat(Rut.normalizar("6000000-k")).contains("6.000.000-K");
        assertThat(Rut.normalizar("76.123.456-0")).contains("76.123.456-0");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12.345.678-9", "76.123.456-7", "abc", "1-9", "", "123456789012-3"})
    void normalizar_rutInvalido_quedaVacio(String texto) {
        assertThat(Rut.normalizar(texto)).isEmpty();
    }

    @Test
    void normalizar_nulo_quedaVacio() {
        assertThat(Rut.normalizar(null)).isEmpty();
    }
}
