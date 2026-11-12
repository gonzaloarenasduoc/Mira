package cl.duoc.mira.documentos.controller;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.documentos.dto.AdjuntarDocumentoRequest;
import cl.duoc.mira.documentos.dto.ArchivoDocumento;
import cl.duoc.mira.documentos.dto.DocumentoResponse;
import cl.duoc.mira.documentos.entity.TipoDocumento;
import cl.duoc.mira.documentos.service.DocumentoService;
import cl.duoc.mira.operaciones.dto.ArriendoResponse;
import cl.duoc.mira.operaciones.dto.VentaResponse;
import cl.duoc.mira.operaciones.service.ArriendoService;
import cl.duoc.mira.operaciones.service.VentaService;
import cl.duoc.mira.usuarios.entity.RolUsuario;
import cl.duoc.mira.usuarios.service.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Adjuntar documentos a un arriendo o a una venta (HU-23) y descargarlos (HU-24). Las rutas cuelgan de la operación, así que
 * los permisos de E008 se apoyan en los de cada operación: el encargado de bodega adjunta a arriendos,
 * pero no a ventas, porque no tiene acceso a ellas.
 */
@Controller
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;
    private final ArriendoService arriendoService;
    private final VentaService ventaService;
    private final Clock clock;

    @ModelAttribute("tipos")
    public TipoDocumento[] tipos() {
        return TipoDocumento.values();
    }

    @GetMapping("/arriendos/{id}/documentos/nuevo")
    public String nuevoParaArriendo(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        cargarArriendo(actual, id, model);
        model.addAttribute("documento", nuevo());
        return "documentos/formulario";
    }

    @PostMapping("/arriendos/{id}/documentos")
    public String adjuntarAArriendo(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                                    @Valid @ModelAttribute("documento") AdjuntarDocumentoRequest documento,
                                    BindingResult errores, @RequestParam(required = false) MultipartFile archivo,
                                    Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                DocumentoResponse adjunto = documentoService.adjuntarAArriendo(actual.empresaId(), id, documento, archivo);
                redirect.addFlashAttribute("exito", mensaje(adjunto));
                return "redirect:/arriendos/" + id;
            } catch (NegocioException ex) {
                errores.reject("negocio", ex.getMessage());
            }
        }
        cargarArriendo(actual, id, model);
        return "documentos/formulario";
    }

    @GetMapping("/ventas/{id}/documentos/nuevo")
    public String nuevoParaVenta(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id, Model model) {
        cargarVenta(actual, id, model);
        model.addAttribute("documento", nuevo());
        return "documentos/formulario";
    }

    @PostMapping("/ventas/{id}/documentos")
    public String adjuntarAVenta(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id,
                                 @Valid @ModelAttribute("documento") AdjuntarDocumentoRequest documento,
                                 BindingResult errores, @RequestParam(required = false) MultipartFile archivo,
                                 Model model, RedirectAttributes redirect) {
        if (!errores.hasErrors()) {
            try {
                DocumentoResponse adjunto = documentoService.adjuntarAVenta(actual.empresaId(), id, documento, archivo);
                redirect.addFlashAttribute("exito", mensaje(adjunto));
                return "redirect:/ventas/" + id;
            } catch (NegocioException ex) {
                errores.reject("negocio", ex.getMessage());
            }
        }
        cargarVenta(actual, id, model);
        return "documentos/formulario";
    }

    /**
     * Descarga el archivo de un documento (HU-24). Se entrega como adjunto, con el nombre original y el
     * tipo de contenido validado al subirlo; el navegador no lo interpreta como página.
     */
    @GetMapping("/documentos/{id}/archivo")
    public ResponseEntity<Resource> descargar(@AuthenticationPrincipal UsuarioAutenticado actual, @PathVariable Long id) {
        ArchivoDocumento archivo = documentoService.descargar(actual.empresaId(), id,
                actual.tieneRol(RolUsuario.ADMINISTRADOR, RolUsuario.VENDEDOR, RolUsuario.JEFATURA));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .contentLength(archivo.tamanoBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .body(new FileSystemResource(archivo.ruta()));
    }

    private AdjuntarDocumentoRequest nuevo() {
        AdjuntarDocumentoRequest documento = new AdjuntarDocumentoRequest();
        documento.setFechaEmision(LocalDate.now(clock));
        return documento;
    }

    private void cargarArriendo(UsuarioAutenticado actual, Long id, Model model) {
        ArriendoResponse a = arriendoService.ficha(actual.empresaId(), id);
        model.addAttribute("seccion", "Arriendos");
        model.addAttribute("operacion", "Arriendo N° " + a.id() + " · " + a.cliente());
        model.addAttribute("volver", "/arriendos/" + id);
        model.addAttribute("accion", "/arriendos/" + id + "/documentos");
    }

    private void cargarVenta(UsuarioAutenticado actual, Long id, Model model) {
        VentaResponse v = ventaService.ficha(actual.empresaId(), id);
        model.addAttribute("seccion", "Ventas");
        model.addAttribute("operacion", "Venta N° " + v.id() + " · " + v.cliente());
        model.addAttribute("volver", "/ventas/" + id);
        model.addAttribute("accion", "/ventas/" + id + "/documentos");
    }

    private static String mensaje(DocumentoResponse d) {
        return "Se adjuntó la " + d.tipo().getEtiqueta().toLowerCase(Locale.ROOT) + " N° " + d.numeroFolio() + ".";
    }
}
