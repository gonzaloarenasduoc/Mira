package cl.duoc.mira.documentos.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.documentos.dto.AdjuntarDocumentoRequest;
import cl.duoc.mira.documentos.dto.DocumentoResponse;
import cl.duoc.mira.documentos.entity.Documento;
import cl.duoc.mira.documentos.repository.DocumentoRepository;
import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.operaciones.service.VentaService;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/**
 * Documentos de las operaciones (E008). El sistema no emite documentos tributarios: los almacena y
 * los relaciona con la operación. Se aceptan PDF, JPG y PNG de hasta 10 MB (D22).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentoService {

    static final long TAMANO_MAXIMO = 10L * 1024 * 1024;

    /** Extensión aceptada → tipo de contenido que se guarda y firma con que debe comenzar el archivo. */
    private static final Map<String, Formato> FORMATOS = Map.of(
            "pdf", new Formato("application/pdf", new byte[]{'%', 'P', 'D', 'F'}),
            "jpg", new Formato("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
            "jpeg", new Formato("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
            "png", new Formato("image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}));

    private final DocumentoRepository documentoRepository;
    private final EmpresaRepository empresaRepository;
    private final ArriendoService arriendoService;
    private final VentaService ventaService;
    private final AlmacenArchivos almacen;
    private final Clock clock;

    /** HU-23: adjunta un documento a un arriendo de la empresa. */
    @Transactional
    public DocumentoResponse adjuntarAArriendo(Long empresaId, Long arriendoId, AdjuntarDocumentoRequest request,
                                               MultipartFile archivo) {
        if (!arriendoService.existe(empresaId, arriendoId)) {
            throw new RecursoNoEncontradoException("El arriendo");
        }
        return adjuntar(empresaId, arriendoId, request, archivo);
    }

    /** HU-23: adjunta un documento a una venta de la empresa. */
    @Transactional
    public DocumentoResponse adjuntarAVenta(Long empresaId, Long ventaId, AdjuntarDocumentoRequest request,
                                            MultipartFile archivo) {
        if (!ventaService.existe(empresaId, ventaId)) {
            throw new RecursoNoEncontradoException("La venta");
        }
        return adjuntar(empresaId, ventaId, request, archivo);
    }

    /**
     * Guarda el archivo y su referencia en la misma transacción: si la transacción se revierte, el
     * archivo recién escrito se borra, para no dejar archivos sin documento.
     */
    private DocumentoResponse adjuntar(Long empresaId, Long operacionId, AdjuntarDocumentoRequest request,
                                       MultipartFile archivo) {
        if (request.getFechaEmision().isAfter(LocalDate.now(clock))) {
            throw new NegocioException("La fecha del documento no puede ser futura.");
        }
        if (archivo == null || archivo.isEmpty()) {
            throw new NegocioException("Seleccione el archivo del documento.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new NegocioException("El archivo supera el máximo de 10 MB.");
        }
        String nombre = nombreOriginal(archivo.getOriginalFilename());
        String extension = extension(nombre);
        Formato formato = FORMATOS.get(extension);
        if (formato == null || !comienzaCon(archivo, formato.firma())) {
            throw new NegocioException("El archivo debe ser un PDF o una imagen JPG o PNG.");
        }

        String ruta;
        try (InputStream contenido = archivo.getInputStream()) {
            ruta = almacen.guardar(empresaId, extension, contenido);
        } catch (IOException ex) {
            throw new NegocioException("No se pudo leer el archivo. Intente nuevamente.");
        }
        borrarSiSeRevierte(ruta);
        Documento documento = documentoRepository.save(new Documento(empresaRepository.getReferenceById(empresaId),
                operacionId, request.getTipo(), request.getNumeroFolio().trim(), request.getFechaEmision(),
                nombre, ruta, formato.contentType(), archivo.getSize()));
        return aResponse(documento);
    }

    private void borrarSiSeRevierte(String ruta) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int estado) {
                    if (estado != STATUS_COMMITTED) {
                        almacen.eliminar(ruta);
                    }
                }
            });
        }
    }

    static DocumentoResponse aResponse(Documento d) {
        return new DocumentoResponse(d.getId(), d.getOperacionId(), d.getTipo(), d.getNumeroFolio(),
                d.getFechaEmision(), d.getNombreArchivo(), d.getTamanoBytes());
    }

    /** Solo el nombre final, sin carpetas: algunos navegadores envían la ruta completa del archivo. */
    private static String nombreOriginal(String original) {
        String nombre = original == null ? "" : original.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1).trim();
        if (nombre.isEmpty()) {
            nombre = "documento";
        }
        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }

    private static String extension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    /** Se revisa el contenido, no solo la extensión: un archivo renombrado a .pdf no pasa. */
    private static boolean comienzaCon(MultipartFile archivo, byte[] firma) {
        try (InputStream contenido = archivo.getInputStream()) {
            return Arrays.equals(contenido.readNBytes(firma.length), firma);
        } catch (IOException ex) {
            return false;
        }
    }

    private record Formato(String contentType, byte[] firma) {
    }
}
