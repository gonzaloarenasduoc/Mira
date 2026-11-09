package cl.duoc.mira.documentos.dto;

import cl.duoc.mira.documentos.entity.TipoDocumento;
import java.time.LocalDate;

public record DocumentoResponse(
        Long id,
        Long operacionId,
        TipoDocumento tipo,
        String numeroFolio,
        LocalDate fechaEmision,
        String nombreArchivo,
        long tamanoBytes) {
}
