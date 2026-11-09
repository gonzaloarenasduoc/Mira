package cl.duoc.mira.documentos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.mira.common.entity.Empresa;
import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.documentos.dto.AdjuntarDocumentoRequest;
import cl.duoc.mira.documentos.entity.Documento;
import cl.duoc.mira.documentos.entity.TipoDocumento;
import cl.duoc.mira.documentos.repository.DocumentoRepository;
import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.operaciones.service.VentaService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** HU-23: adjuntar documentos a una operación (D22). */
class DocumentoServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-09T13:00:00Z"), ZoneId.of("America/Santiago"));
    private static final byte[] PDF = "%PDF-1.7 guía".getBytes();

    private DocumentoRepository documentoRepository;
    private ArriendoService arriendoService;
    private VentaService ventaService;
    private AlmacenArchivos almacen;
    private DocumentoService service;

    @BeforeEach
    void preparar() {
        documentoRepository = mock(DocumentoRepository.class);
        arriendoService = mock(ArriendoService.class);
        ventaService = mock(VentaService.class);
        almacen = mock(AlmacenArchivos.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        service = new DocumentoService(documentoRepository, empresaRepository, arriendoService, ventaService, almacen, RELOJ);

        when(empresaRepository.getReferenceById(EMPRESA_ID)).thenReturn(mock(Empresa.class));
        when(arriendoService.existe(EMPRESA_ID, 4L)).thenReturn(true);
        when(ventaService.existe(EMPRESA_ID, 9L)).thenReturn(true);
        when(almacen.guardar(eq(EMPRESA_ID), anyString(), any())).thenReturn("1/generado.pdf");
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void limpiar() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void adjuntarAArriendo_pdfValido_guardaElArchivoYSuReferencia() {
        var respuesta = service.adjuntarAArriendo(EMPRESA_ID, 4L, solicitud(LocalDate.of(2026, 11, 9)),
                new MockMultipartFile("archivo", "C:\\Users\\vendedor\\guia 1045.pdf", "application/octet-stream", PDF));

        ArgumentCaptor<Documento> guardado = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(guardado.capture());
        Documento d = guardado.getValue();
        assertThat(d.getOperacionId()).isEqualTo(4L);
        assertThat(d.getTipo()).isEqualTo(TipoDocumento.GUIA_DESPACHO);
        assertThat(d.getNumeroFolio()).isEqualTo("1045");
        assertThat(d.getNombreArchivo()).as("sin la ruta del equipo del usuario").isEqualTo("guia 1045.pdf");
        assertThat(d.getRutaArchivo()).isEqualTo("1/generado.pdf");
        assertThat(d.getContentType()).as("se guarda el tipo real, no el que declara el navegador").isEqualTo("application/pdf");
        assertThat(d.getTamanoBytes()).isEqualTo(PDF.length);
        assertThat(respuesta.numeroFolio()).isEqualTo("1045");
    }

    @Test
    void adjuntarAVenta_imagenPng_seAcepta() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
        when(almacen.guardar(eq(EMPRESA_ID), eq("png"), any())).thenReturn("1/generado.png");

        service.adjuntarAVenta(EMPRESA_ID, 9L, solicitud(LocalDate.of(2026, 11, 9)),
                new MockMultipartFile("archivo", "factura.PNG", "image/png", png));

        ArgumentCaptor<Documento> guardado = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(guardado.capture());
        assertThat(guardado.getValue().getContentType()).isEqualTo("image/png");
    }

    /** Se revisa el contenido: un archivo cualquiera renombrado a .pdf no pasa. */
    @Test
    void adjuntar_archivoRenombradoAPdf_seRechazaSinGuardar() {
        NegocioException ex = assertThrows(NegocioException.class, () -> service.adjuntarAArriendo(EMPRESA_ID, 4L,
                solicitud(LocalDate.of(2026, 11, 9)), new MockMultipartFile("archivo", "virus.pdf", "application/pdf", "MZ...".getBytes())));

        assertThat(ex.getMessage()).isEqualTo("El archivo debe ser un PDF o una imagen JPG o PNG.");
        verify(almacen, never()).guardar(anyLong(), anyString(), any());
    }

    @Test
    void adjuntar_formatoNoPermitido_seRechaza() {
        assertThrows(NegocioException.class, () -> service.adjuntarAArriendo(EMPRESA_ID, 4L, solicitud(LocalDate.of(2026, 11, 9)),
                new MockMultipartFile("archivo", "guia.docx", "application/msword", PDF)));
        verify(documentoRepository, never()).save(any());
    }

    @Test
    void adjuntar_archivoSobre10Mb_seRechaza() {
        byte[] grande = new byte[(int) DocumentoService.TAMANO_MAXIMO + 1];
        System.arraycopy(PDF, 0, grande, 0, PDF.length);

        NegocioException ex = assertThrows(NegocioException.class, () -> service.adjuntarAArriendo(EMPRESA_ID, 4L,
                solicitud(LocalDate.of(2026, 11, 9)), new MockMultipartFile("archivo", "guia.pdf", "application/pdf", grande)));
        assertThat(ex.getMessage()).isEqualTo("El archivo supera el máximo de 10 MB.");
    }

    @Test
    void adjuntar_sinArchivo_seRechaza() {
        NegocioException ex = assertThrows(NegocioException.class,
                () -> service.adjuntarAArriendo(EMPRESA_ID, 4L, solicitud(LocalDate.of(2026, 11, 9)), null));
        assertThat(ex.getMessage()).isEqualTo("Seleccione el archivo del documento.");
    }

    @Test
    void adjuntar_fechaFutura_seRechaza() {
        NegocioException ex = assertThrows(NegocioException.class, () -> service.adjuntarAArriendo(EMPRESA_ID, 4L,
                solicitud(LocalDate.of(2026, 11, 10)), new MockMultipartFile("archivo", "guia.pdf", "application/pdf", PDF)));
        assertThat(ex.getMessage()).isEqualTo("La fecha del documento no puede ser futura.");
    }

    /** PNF-06: no se adjunta a una operación de otra empresa, ni a una venta por la ruta de arriendos. */
    @Test
    void adjuntar_operacionQueNoEsDelTipoNiDeLaEmpresa_lanzaNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> service.adjuntarAArriendo(EMPRESA_ID, 9L,
                solicitud(LocalDate.of(2026, 11, 9)), new MockMultipartFile("archivo", "guia.pdf", "application/pdf", PDF)));
        verify(almacen, never()).guardar(anyLong(), anyString(), any());
    }

    /** Si la transacción se revierte después de escribir el archivo, el archivo se borra. */
    @Test
    void adjuntar_transaccionRevertida_borraElArchivo() {
        TransactionSynchronizationManager.initSynchronization();

        service.adjuntarAArriendo(EMPRESA_ID, 4L, solicitud(LocalDate.of(2026, 11, 9)),
                new MockMultipartFile("archivo", "guia.pdf", "application/pdf", PDF));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(almacen).eliminar("1/generado.pdf");
    }

    private static AdjuntarDocumentoRequest solicitud(LocalDate fecha) {
        AdjuntarDocumentoRequest r = new AdjuntarDocumentoRequest();
        r.setTipo(TipoDocumento.GUIA_DESPACHO);
        r.setNumeroFolio(" 1045 ");
        r.setFechaEmision(fecha);
        return r;
    }
}
