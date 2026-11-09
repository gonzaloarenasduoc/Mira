package cl.duoc.mira.operaciones.service;

import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.OperacionClienteResponse;
import cl.duoc.mira.operaciones.entity.Arriendo;
import cl.duoc.mira.operaciones.entity.Operacion;
import cl.duoc.mira.operaciones.entity.OperacionDetalle;
import cl.duoc.mira.operaciones.entity.Venta;
import cl.duoc.mira.operaciones.repository.ArriendoRepository;
import cl.duoc.mira.operaciones.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Historial de operaciones de un cliente (HU-19): sus ventas y arriendos, del más reciente al más
 * antiguo. Va aparte de ClienteService porque ArriendoService y VentaService ya dependen de él.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistorialClienteService {

    private final ClienteService clienteService;
    private final ArriendoRepository arriendoRepository;
    private final VentaRepository ventaRepository;
    private final EquipoService equipoService;
    private final Clock clock;

    /**
     * Las ventas se incluyen solo si el rol tiene acceso a E006: el encargado de bodega ve únicamente
     * los arriendos (matriz de permisos). Un cliente de otra empresa se responde como inexistente.
     */
    public List<OperacionClienteResponse> historial(Long empresaId, Long clienteId, boolean incluirVentas) {
        clienteService.obtener(empresaId, clienteId);
        List<Arriendo> arriendos = arriendoRepository.delCliente(empresaId, clienteId);
        List<Venta> ventas = incluirVentas ? ventaRepository.delCliente(empresaId, clienteId) : List.of();
        Set<Long> ids = Stream.concat(arriendos.stream(), ventas.stream())
                .flatMap(o -> o.getDetalles().stream())
                .map(OperacionDetalle::getEquipoId).collect(Collectors.toSet());
        Map<Long, EquipoResponse> equipos = equipoService.resumenPorId(empresaId, ids);
        LocalDate hoy = LocalDate.now(clock);

        Stream<OperacionClienteResponse> filasArriendo = arriendos.stream()
                .map(a -> new OperacionClienteResponse(a.getId(), false, a.getFechaInicio(), series(a, equipos),
                        ArriendoService.estado(a, hoy), ArriendoService.total(a, hoy),
                        a.getDetalles().stream().anyMatch(OperacionDetalle::estaPendienteDeRetorno)));
        Stream<OperacionClienteResponse> filasVenta = ventas.stream()
                .map(v -> new OperacionClienteResponse(v.getId(), true, v.getFecha(), series(v, equipos), null,
                        v.getDetalles().stream().map(OperacionDetalle::getValorUnitario).reduce(BigDecimal.ZERO, BigDecimal::add),
                        false));
        return Stream.concat(filasArriendo, filasVenta)
                .sorted(Comparator.comparing(OperacionClienteResponse::fecha)
                        .thenComparing(OperacionClienteResponse::id).reversed())
                .toList();
    }

    private static List<String> series(Operacion o, Map<Long, EquipoResponse> equipos) {
        return o.getDetalles().stream()
                .map(d -> equipos.containsKey(d.getEquipoId()) ? equipos.get(d.getEquipoId()).numeroSerie() : "—")
                .toList();
    }
}
