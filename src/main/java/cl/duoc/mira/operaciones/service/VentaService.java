package cl.duoc.mira.operaciones.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.common.exception.RecursoNoEncontradoException;
import cl.duoc.mira.common.repository.EmpresaRepository;
import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.LineaVentaRequest;
import cl.duoc.mira.operaciones.dto.LineaVentaResponse;
import cl.duoc.mira.operaciones.dto.RegistrarVentaRequest;
import cl.duoc.mira.operaciones.dto.VentaResponse;
import cl.duoc.mira.operaciones.entity.Cliente;
import cl.duoc.mira.operaciones.entity.OperacionDetalle;
import cl.duoc.mira.operaciones.entity.Venta;
import cl.duoc.mira.operaciones.repository.ArriendoRepository;
import cl.duoc.mira.operaciones.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro de ventas (E006). Registrar una venta crea la operación y sus líneas, deja cada equipo
 * vendido y registra su movimiento: todo en una sola transacción (03-modelo-datos.md, sección 5).
 * Los equipos se modifican solo a través de EquipoService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ArriendoRepository arriendoRepository;
    private final ClienteService clienteService;
    private final EquipoService equipoService;
    private final EmpresaRepository empresaRepository;
    private final Clock clock;

    /**
     * Registra la venta (HU-18, CF-09). Si cualquier línea falla, la excepción revierte la transacción
     * completa: no queda la venta, ni equipos vendidos, ni movimientos.
     */
    @Transactional
    public VentaResponse registrar(Long empresaId, Long usuarioId, RegistrarVentaRequest request) {
        List<LineaVentaRequest> lineas = request.getLineas().stream().filter(LineaVentaRequest::isIncluir).toList();
        validar(request, lineas, LocalDate.now(clock));
        Cliente cliente = clienteService.obtener(empresaId, request.getClienteId());
        if (!cliente.isActivo()) {
            throw new NegocioException("El cliente " + cliente.getRazonSocial() + " está inactivo.");
        }
        Set<Long> ids = lineas.stream().map(LineaVentaRequest::getEquipoId).collect(Collectors.toSet());
        Map<Long, EquipoResponse> equipos = equipoService.resumenPorId(empresaId, ids);
        if (equipos.size() != ids.size()) {
            throw new RecursoNoEncontradoException("El equipo");
        }
        for (Long id : ids) {
            if (!arriendoRepository.vigentesDelEquipo(empresaId, id).isEmpty()) {
                throw new NegocioException("El equipo " + equipos.get(id).numeroSerie()
                        + " no se puede vender porque tiene un arriendo vigente.");
            }
        }

        Venta venta = new Venta(empresaRepository.getReferenceById(empresaId), cliente, usuarioId,
                request.getFecha(), vacioANulo(request.getNumeroOrdenCompra()), vacioANulo(request.getObservaciones()));
        lineas.forEach(l -> venta.agregarLinea(l.getEquipoId(), l.getValor()));
        ventaRepository.save(venta);

        lineas.forEach(l -> equipoService.despacharEnVenta(empresaId, usuarioId, l.getEquipoId(), venta.getId()));
        return aResponse(empresaId, venta);
    }

    /** Si el venta existe y es de la empresa; lo usan otros módulos, como documentos (HU-23). */
    public boolean existe(Long empresaId, Long id) {
        return ventaRepository.existsByIdAndEmpresaId(id, empresaId);
    }

    /** Una venta de otra empresa se responde como inexistente (PNF-06). */
    public VentaResponse ficha(Long empresaId, Long id) {
        return aResponse(empresaId, ventaRepository.buscarFicha(empresaId, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La venta")));
    }

    private static void validar(RegistrarVentaRequest request, List<LineaVentaRequest> lineas, LocalDate hoy) {
        if (lineas.isEmpty()) {
            throw new NegocioException("Seleccione al menos un equipo para vender.");
        }
        if (request.getFecha().isAfter(hoy)) {
            throw new NegocioException("La fecha de la venta no puede ser futura.");
        }
        Set<Long> vistos = new HashSet<>();
        for (LineaVentaRequest linea : lineas) {
            if (linea.getEquipoId() == null || !vistos.add(linea.getEquipoId())) {
                throw new NegocioException("Cada equipo puede incluirse una sola vez en la venta.");
            }
            if (linea.getValor() == null || linea.getValor().signum() <= 0) {
                throw new NegocioException("Indique un valor de venta mayor que cero para cada equipo seleccionado.");
            }
        }
    }

    private VentaResponse aResponse(Long empresaId, Venta v) {
        Set<Long> ids = v.getDetalles().stream().map(OperacionDetalle::getEquipoId).collect(Collectors.toSet());
        Map<Long, EquipoResponse> equipos = equipoService.resumenPorId(empresaId, ids);
        List<LineaVentaResponse> lineas = v.getDetalles().stream()
                .map(d -> {
                    EquipoResponse e = equipos.get(d.getEquipoId());
                    return new LineaVentaResponse(d.getEquipoId(),
                            e == null ? "—" : e.numeroSerie(),
                            e == null ? "—" : e.categoria() + " " + e.marca() + " " + e.modelo(),
                            d.getValorUnitario());
                })
                .toList();
        BigDecimal total = lineas.stream().map(LineaVentaResponse::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new VentaResponse(v.getId(), v.getCliente().getRazonSocial(), v.getCliente().getRut(),
                v.getFecha(), v.getNumeroOrdenCompra(), v.getObservaciones(), lineas, total);
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
