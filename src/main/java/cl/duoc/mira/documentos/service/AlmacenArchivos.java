package cl.duoc.mira.documentos.service;

import cl.duoc.mira.common.exception.NegocioException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Guarda los archivos adjuntos en disco, bajo APP_UPLOAD_DIR (D06). Cada archivo recibe un nombre
 * generado dentro de la carpeta de su empresa: el nombre que trae el usuario nunca se usa como ruta.
 */
@Component
public class AlmacenArchivos {

    private final Path base;

    public AlmacenArchivos(@Value("${app.upload-dir}") String directorio) {
        this.base = Path.of(directorio).toAbsolutePath().normalize();
    }

    /** Devuelve la ruta relativa a la carpeta base, que es lo que se guarda en la base de datos. */
    public String guardar(Long empresaId, String extension, InputStream contenido) {
        String relativa = empresaId + "/" + UUID.randomUUID() + "." + extension;
        Path destino = resolver(relativa);
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(contenido, destino);
        } catch (IOException ex) {
            throw new NegocioException("No se pudo guardar el archivo. Intente nuevamente.");
        }
        return relativa;
    }

    public void eliminar(String rutaRelativa) {
        try {
            Files.deleteIfExists(resolver(rutaRelativa));
        } catch (IOException ex) {
            // Un archivo huérfano no afecta los datos: queda sin referencia en la base.
        }
    }

    /** Ruta absoluta del archivo; rechaza cualquier ruta que salga de la carpeta base. */
    public Path resolver(String rutaRelativa) {
        Path ruta = base.resolve(rutaRelativa).normalize();
        if (!ruta.startsWith(base)) {
            throw new IllegalArgumentException("Ruta de archivo fuera de la carpeta de documentos.");
        }
        return ruta;
    }
}
