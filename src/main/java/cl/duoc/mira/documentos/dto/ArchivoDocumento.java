package cl.duoc.mira.documentos.dto;

import java.nio.file.Path;

/** Archivo de un documento listo para descargar (HU-24). */
public record ArchivoDocumento(String nombre, String contentType, long tamanoBytes, Path ruta) {
}
