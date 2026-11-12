package cl.duoc.mira.documentos.dto;

import cl.duoc.mira.documentos.entity.TipoDocumento;
import java.time.LocalDate;

/** {@code venta} indica si la operación del documento es una venta; si no, es un arriendo. */
public record DocumentoResponse(
        Long id,
        Long operacionId,
        boolean venta,
        TipoDocumento tipo,
        String numeroFolio,
        LocalDate fechaEmision,
        String nombreArchivo,
        long tamanoBytes) {
}
