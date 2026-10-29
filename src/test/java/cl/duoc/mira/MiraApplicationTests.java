package cl.duoc.mira;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cl.duoc.mira.inventario.dto.FiltroEquipos;
import cl.duoc.mira.inventario.dto.RegistrarTrasladoRequest;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.inventario.service.TrasladoService;
import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.operaciones.dto.LineaArriendoRequest;
import cl.duoc.mira.operaciones.dto.RegistrarArriendoRequest;
import cl.duoc.mira.operaciones.dto.RegistrarRetornoRequest;
import cl.duoc.mira.operaciones.repository.ArriendoRepository;
import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.operaciones.repository.ArriendoVigente;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
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
    @Autowired
    private TrasladoService trasladoService;
    @Autowired
    private EquipoService equipoService;
    @Autowired
    private ArriendoRepository arriendoRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ArriendoService arriendoService;
    /** El reloj de la aplicación (America/Santiago): "hoy" en UTC puede ser otro día. */
    @Autowired
    private Clock reloj;

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

    /** CF-08 contra la base real: la llegada reasigna la bodega y quedan los dos movimientos. */
    @Test
    void traslado_salidaYLlegada_dejaElEquipoEnElDestinoConSusMovimientos() {
        long empresa = crearEmpresa("55.555.555-5");
        insertarEquipo(empresa, "SN-0003");
        long equipo = idEquipo(empresa, "SN-0003");
        long destino = jdbc.queryForObject(
                "INSERT INTO bodega (empresa_id, nombre) VALUES (?, 'Bodega Sur') RETURNING id", Long.class, empresa);
        RegistrarTrasladoRequest solicitud = new RegistrarTrasladoRequest();
        solicitud.setEquipoId(equipo);
        solicitud.setBodegaDestinoId(destino);

        trasladoService.registrarSalida(empresa, administrador(), solicitud);
        entityManager.flush();
        assertThat(estadoYBodega(equipo)).isEqualTo("EN_TRANSITO/Bodega de prueba");
        trasladoService.confirmarLlegada(empresa, administrador(), equipo);
        entityManager.flush();

        assertThat(estadoYBodega(equipo)).isEqualTo("DISPONIBLE/Bodega Sur");
        assertThat(jdbc.queryForList("SELECT tipo FROM movimiento WHERE equipo_id = ? ORDER BY id", String.class, equipo))
                .containsExactly("TRASLADO_SALIDA", "TRASLADO_LLEGADA");
    }

    /** PNF-07 contra la base real: comodines e inyección se buscan como texto literal. */
    @Test
    void buscar_textoConComodinesOInyeccion_noAlteraElResultado() {
        long empresa = crearEmpresa("66.666.666-6");
        insertarEquipo(empresa, "SN-0004");

        assertThat(equipoService.buscar(empresa, texto("marc"))).hasSize(1);
        assertThat(equipoService.buscar(empresa, texto("%"))).isEmpty();
        assertThat(equipoService.buscar(empresa, texto("_"))).isEmpty();
        assertThat(equipoService.buscar(empresa, texto("' OR 1=1 --"))).isEmpty();
    }

    /** HU-13: solo las líneas de arriendo sin retorno registrado cuentan como vigentes. */
    @Test
    void arriendosVigentes_soloIncluyeLineasSinRetorno() {
        long empresa = crearEmpresa("77.777.777-7");
        insertarEquipo(empresa, "SN-0005");
        insertarEquipo(empresa, "SN-0006");
        long cliente = jdbc.queryForObject(
                "INSERT INTO cliente (empresa_id, rut, razon_social) VALUES (?, '76.000.000-0', 'Minera de prueba') RETURNING id",
                Long.class, empresa);
        long operacion = jdbc.queryForObject("""
                INSERT INTO operacion (empresa_id, tipo, cliente_id, usuario_id, fecha)
                VALUES (?, 'ARRIENDO', ?, ?, DATE '2026-03-01') RETURNING id
                """, Long.class, empresa, cliente, administrador());
        jdbc.update("INSERT INTO arriendo (id, fecha_inicio, fecha_retorno_comprometida) VALUES (?, DATE '2026-03-01', DATE '2026-03-25')",
                operacion);
        jdbc.update("""
                INSERT INTO operacion_detalle (operacion_id, equipo_id, valor_unitario, fecha_retorno_real)
                SELECT ?, id, 50000, CASE WHEN numero_serie = 'SN-0006' THEN DATE '2026-03-20' END
                FROM equipo WHERE empresa_id = ?
                """, operacion, empresa);
        long vigente = idEquipo(empresa, "SN-0005");

        assertThat(arriendoRepository.vigentes(empresa))
                .containsExactly(new ArriendoVigente(vigente, LocalDate.of(2026, 3, 25)));
    }

    /** CF-01 contra la base real: el arriendo queda guardado y el equipo, arrendado con su movimiento. */
    @Test
    void arriendo_equipoDisponible_quedaArrendadoConSuMovimiento() {
        long empresa = crearEmpresa("88.888.888-8");
        insertarEquipo(empresa, "SN-0007");
        long equipo = idEquipo(empresa, "SN-0007");

        arriendoService.registrar(empresa, administrador(), arriendo(crearCliente(empresa), equipo));
        entityManager.flush();

        assertThat(estadoYBodega(equipo)).isEqualTo("ARRENDADO/Bodega de prueba");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM operacion_detalle WHERE equipo_id = ?", Integer.class, equipo))
                .isEqualTo(1);
        assertThat(jdbc.queryForList("SELECT tipo FROM movimiento WHERE equipo_id = ? AND operacion_id IS NOT NULL",
                String.class, equipo)).containsExactly("SALIDA_ARRIENDO");
    }

    /**
     * CF-03 contra la base real: si la segunda línea falla, no queda nada guardado. Corre fuera de
     * la transacción de la prueba para observar la reversión que hace el servicio.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void arriendo_segundaLineaNoDisponible_noGuardaNada() {
        long empresa = crearEmpresa("99.999.999-9");
        insertarEquipo(empresa, "SN-0008");
        insertarEquipo(empresa, "SN-0009");
        long primero = idEquipo(empresa, "SN-0008");
        long segundo = idEquipo(empresa, "SN-0009");
        jdbc.update("UPDATE equipo SET estado = 'EN_MANTENCION' WHERE id = ?", segundo);
        long cliente = crearCliente(empresa);

        assertThrows(NegocioException.class,
                () -> arriendoService.registrar(empresa, administrador(), arriendo(cliente, primero, segundo)));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM operacion WHERE empresa_id = ?", Integer.class, empresa))
                .isZero();
        assertThat(estadoYBodega(primero)).isEqualTo("DISPONIBLE/Bodega de prueba");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM movimiento WHERE equipo_id IN (?, ?)", Integer.class,
                primero, segundo)).isZero();
    }

    /** CF-04 contra la base real: retorno en otra bodega, sin traslado adicional. */
    @Test
    void retorno_enOtraBodega_reasignaElEquipoYCierraLaLinea() {
        long empresa = crearEmpresa("12.121.212-1");
        insertarEquipo(empresa, "SN-0010");
        long equipo = idEquipo(empresa, "SN-0010");
        long sur = jdbc.queryForObject(
                "INSERT INTO bodega (empresa_id, nombre) VALUES (?, 'Bodega Sur') RETURNING id", Long.class, empresa);
        RegistrarArriendoRequest solicitud = arriendo(crearCliente(empresa), equipo);
        solicitud.setFechaInicio(LocalDate.now(reloj));
        solicitud.setFechaRetornoComprometida(LocalDate.now(reloj).plusDays(15));
        long arriendoId = arriendoService.registrar(empresa, administrador(), solicitud).id();

        RegistrarRetornoRequest retorno = new RegistrarRetornoRequest();
        retorno.setEquipoId(equipo);
        retorno.setBodegaId(sur);
        retorno.setFechaRetorno(LocalDate.now(reloj));
        arriendoService.registrarRetorno(empresa, administrador(), arriendoId, retorno);
        entityManager.flush();

        assertThat(estadoYBodega(equipo)).isEqualTo("DISPONIBLE/Bodega Sur");
        assertThat(jdbc.queryForObject("SELECT fecha_retorno_real FROM operacion_detalle WHERE equipo_id = ?",
                LocalDate.class, equipo)).isEqualTo(LocalDate.now(reloj));
        assertThat(jdbc.queryForList("SELECT tipo FROM movimiento WHERE equipo_id = ? ORDER BY id", String.class, equipo))
                .containsExactly("SALIDA_ARRIENDO", "RETORNO_ARRIENDO");
        assertThat(jdbc.queryForObject(
                "SELECT bodega_destino_id FROM movimiento WHERE equipo_id = ? AND tipo = 'RETORNO_ARRIENDO'",
                Long.class, equipo)).isEqualTo(sur);
    }

    /**
     * CF-10 contra la base real: entran las líneas sin retorno que vencen en 3 días o menos, incluidas
     * las vencidas; no entran las ya retornadas ni las que vencen más adelante.
     */
    @Test
    void alertasDeRetorno_soloLineasPendientesQueVencenPronto() {
        long empresa = crearEmpresa("13.131.313-1");
        for (String serie : List.of("SN-0011", "SN-0012", "SN-0013", "SN-0014")) {
            insertarEquipo(empresa, serie);
        }
        long cliente = crearCliente(empresa);
        arriendoDirecto(empresa, cliente, "SN-0011", LocalDate.now(reloj).minusDays(4), null);
        arriendoDirecto(empresa, cliente, "SN-0012", LocalDate.now(reloj).minusDays(4), LocalDate.now(reloj).minusDays(1));
        arriendoDirecto(empresa, cliente, "SN-0013", LocalDate.now(reloj).plusDays(2), null);
        arriendoDirecto(empresa, cliente, "SN-0014", LocalDate.now(reloj).plusDays(10), null);

        assertThat(arriendoService.alertasDeRetorno(empresa))
                .extracting(a -> a.numeroSerie() + "/" + a.dias())
                .containsExactly("SN-0011/-4", "SN-0013/2");
    }

    private void arriendoDirecto(long empresa, long cliente, String serie, LocalDate retorno, LocalDate retornoReal) {
        long operacion = jdbc.queryForObject("""
                INSERT INTO operacion (empresa_id, tipo, cliente_id, usuario_id, fecha)
                VALUES (?, 'ARRIENDO', ?, ?, ?) RETURNING id
                """, Long.class, empresa, cliente, administrador(), retorno.minusDays(10));
        jdbc.update("INSERT INTO arriendo (id, fecha_inicio, fecha_retorno_comprometida) VALUES (?, ?, ?)",
                operacion, retorno.minusDays(10), retorno);
        jdbc.update("INSERT INTO operacion_detalle (operacion_id, equipo_id, valor_unitario, fecha_retorno_real) VALUES (?, ?, 50000, ?)",
                operacion, idEquipo(empresa, serie), retornoReal);
    }

    private long crearCliente(long empresaId) {
        return jdbc.queryForObject(
                "INSERT INTO cliente (empresa_id, rut, razon_social) VALUES (?, '76.111.111-1', 'Cliente de prueba') RETURNING id",
                Long.class, empresaId);
    }

    private static RegistrarArriendoRequest arriendo(long clienteId, Long... equipos) {
        RegistrarArriendoRequest r = new RegistrarArriendoRequest();
        r.setClienteId(clienteId);
        r.setFechaInicio(LocalDate.of(2026, 3, 10));
        r.setFechaRetornoComprometida(LocalDate.of(2026, 3, 25));
        r.setLineas(List.of(equipos).stream()
                .map(id -> new LineaArriendoRequest(id, true, new BigDecimal("50000"))).toList());
        return r;
    }

    private long idEquipo(long empresaId, String numeroSerie) {
        return jdbc.queryForObject("SELECT id FROM equipo WHERE empresa_id = ? AND numero_serie = ?",
                Long.class, empresaId, numeroSerie);
    }

    private long administrador() {
        return jdbc.queryForObject("SELECT id FROM usuario WHERE email = 'admin@mira.cl'", Long.class);
    }

    private String estadoYBodega(long equipo) {
        return jdbc.queryForObject(
                "SELECT e.estado || '/' || b.nombre FROM equipo e JOIN bodega b ON b.id = e.bodega_id WHERE e.id = ?",
                String.class, equipo);
    }

    private static FiltroEquipos texto(String texto) {
        FiltroEquipos filtro = new FiltroEquipos();
        filtro.setTexto(texto);
        return filtro;
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
