package cl.duoc.mira;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * No prueba negocio: verifica que Surefire y JUnit 5 estan bien cableados,
 * sin levantar el contexto de Spring ni la base de datos.
 */
class EntornoPruebasTest {

    @Test
    void junit_estaConfigurado_ejecutaLaPrueba() {
        assertTrue(true);
    }
}
