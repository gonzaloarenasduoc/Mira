package cl.duoc.mira.documentos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** D06: los adjuntos se guardan en disco con un nombre generado, dentro de la carpeta de su empresa. */
class AlmacenArchivosTest {

    @TempDir
    Path carpeta;

    @Test
    void guardar_creaElArchivoConNombreGeneradoEnLaCarpetaDeLaEmpresa() throws Exception {
        AlmacenArchivos almacen = new AlmacenArchivos(carpeta.toString());

        String ruta = almacen.guardar(3L, "pdf", new ByteArrayInputStream("%PDF-1.7".getBytes()));

        assertThat(ruta).startsWith("3/").endsWith(".pdf");
        assertThat(Files.readString(carpeta.resolve(ruta))).isEqualTo("%PDF-1.7");
    }

    @Test
    void eliminar_borraElArchivo() {
        AlmacenArchivos almacen = new AlmacenArchivos(carpeta.toString());
        String ruta = almacen.guardar(3L, "png", new ByteArrayInputStream(new byte[]{1, 2, 3}));

        almacen.eliminar(ruta);

        assertThat(carpeta.resolve(ruta)).doesNotExist();
    }

    /** Una ruta guardada no puede apuntar fuera de la carpeta de documentos. */
    @Test
    void resolver_rutaQueSaleDeLaCarpeta_seRechaza() {
        AlmacenArchivos almacen = new AlmacenArchivos(carpeta.toString());

        assertThrows(IllegalArgumentException.class, () -> almacen.resolver("../../etc/passwd"));
    }
}
