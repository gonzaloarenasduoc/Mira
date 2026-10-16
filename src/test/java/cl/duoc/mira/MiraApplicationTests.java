package cl.duoc.mira;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Levanta la aplicación contra un PostgreSQL 16 real y verifica el esquema que dejan las
 * migraciones de Flyway. No se usa una base en memoria porque se comporta distinto a
 * PostgreSQL (docs/07-pruebas.md). Sin Docker disponible, la clase se omite.
 */
@SpringBootTest
@Transactional
@Testcontainers(disabledWithoutDocker = true)
class MiraApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void arranque_conBaseVacia_aplicaLasDosMigraciones() {
        Integer aplicadas = jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success", Integer.class);

        assertThat(aplicadas).isEqualTo(2);
    }

    @Test
    void datosIniciales_administrador_quedaConContrasenaCifradaConBCrypt() {
        String hash = jdbc.queryForObject(
                "SELECT password_hash FROM usuario WHERE email = 'admin@mira.cl'", String.class);

        assertThat(hash).startsWith("$2a$").isNotEqualTo("admin123");
    }

    @Test
    void equipo_numeroSerieRepetidoEnLaMismaEmpresa_esRechazado() {
        long empresa = crearEmpresa("22.222.222-2");
        insertarEquipo(empresa, "SN-0001");

        assertThrows(DataIntegrityViolationException.class, () -> insertarEquipo(empresa, "SN-0001"));
    }

    @Test
    void equipo_numeroSerieRepetidoEnOtraEmpresa_esAceptado() {
        insertarEquipo(crearEmpresa("33.333.333-3"), "SN-0002");
        long otraEmpresa = crearEmpresa("44.444.444-4");

        assertDoesNotThrow(() -> insertarEquipo(otraEmpresa, "SN-0002"));
    }

    private long crearEmpresa(String rut) {
        long id = jdbc.queryForObject(
                "INSERT INTO empresa (rut, razon_social) VALUES (?, 'Empresa de prueba') RETURNING id",
                Long.class, rut);
        jdbc.update("INSERT INTO bodega (empresa_id, nombre) VALUES (?, 'Bodega de prueba')", id);
        jdbc.update("INSERT INTO categoria (empresa_id, nombre) VALUES (?, 'Categoría de prueba')", id);
        return id;
    }

    private void insertarEquipo(long empresaId, String numeroSerie) {
        jdbc.update("""
                INSERT INTO equipo (empresa_id, categoria_id, bodega_id, numero_serie, marca, modelo, estado)
                SELECT ?, c.id, b.id, ?, 'Marca', 'Modelo', 'DISPONIBLE'
                FROM categoria c JOIN bodega b ON b.empresa_id = c.empresa_id
                WHERE c.empresa_id = ?
                """, empresaId, numeroSerie, empresaId);
    }
}
