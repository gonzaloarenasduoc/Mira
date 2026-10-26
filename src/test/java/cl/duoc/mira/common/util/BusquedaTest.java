package cl.duoc.mira.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** HU-11 y PNF-07: el texto del usuario se busca literalmente, sin comodines propios. */
class BusquedaTest {

    @Test
    void contiene_textoVacioONulo_noFiltra() {
        assertThat(Busqueda.contiene(null)).isEqualTo("%");
        assertThat(Busqueda.contiene("   ")).isEqualTo("%");
    }

    @Test
    void contiene_textoNormal_quedaEnMinusculasYSinEspaciosExtremos() {
        assertThat(Busqueda.contiene("  Leica GS18 ")).isEqualTo("%leica gs18%");
    }

    @Test
    void contiene_comodines_seEscapanParaBuscarseLiteralmente() {
        assertThat(Busqueda.contiene("50%_a\\b")).isEqualTo("%50\\%\\_a\\\\b%");
    }
}
