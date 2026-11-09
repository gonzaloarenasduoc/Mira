package cl.duoc.mira.indicadores.service;

import cl.duoc.mira.indicadores.dto.PanelInventario;
import cl.duoc.mira.inventario.dto.ConteoEquipos;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.AlertaRetornoResponse;
import cl.duoc.mira.operaciones.service.ArriendoService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Panel de indicadores (E009). Solo lee: los datos vienen de los servicios de los otros módulos, que
 * calculan sin almacenar (02-arquitectura.md).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndicadoresService {

    /** Estados que cuentan como inventario; el vendido ya no está en ninguna bodega (D23). */
    private static final List<EstadoEquipo> EN_INVENTARIO =
            List.of(EstadoEquipo.DISPONIBLE, EstadoEquipo.ARRENDADO, EstadoEquipo.EN_TRANSITO, EstadoEquipo.EN_MANTENCION);

    private final ArriendoService arriendoService;
    private final EquipoService equipoService;

    /**
     * Equipos con retorno vencido, del más atrasado al menos atrasado (HU-28, CF-10). Reutiliza las
     * alertas de HU-16 y deja fuera los que todavía no vencen.
     */
    public List<AlertaRetornoResponse> retornosVencidos(Long empresaId) {
        return arriendoService.alertasDeRetorno(empresaId).stream()
                .filter(AlertaRetornoResponse::vencido)
                .toList();
    }

    /**
     * Equipos por bodega y por estado (HU-26). Una sola consulta agregada; aquí solo se ordena en
     * series para los gráficos. Las bodegas sin equipos en inventario no aparecen.
     */
    public PanelInventario inventario(Long empresaId) {
        List<ConteoEquipos> conteos = equipoService.conteoPorBodegaYEstado(empresaId);
        long vendidos = conteos.stream().filter(c -> c.estado() == EstadoEquipo.VENDIDO).mapToLong(ConteoEquipos::cantidad).sum();
        List<ConteoEquipos> enInventario = conteos.stream().filter(c -> EN_INVENTARIO.contains(c.estado())).toList();
        List<String> bodegas = enInventario.stream().map(ConteoEquipos::bodega).distinct().sorted().toList();
        Map<String, Map<EstadoEquipo, Long>> porBodega = enInventario.stream().collect(Collectors.groupingBy(
                ConteoEquipos::bodega, Collectors.toMap(ConteoEquipos::estado, ConteoEquipos::cantidad)));
        List<PanelInventario.SerieEstado> series = EN_INVENTARIO.stream()
                .map(estado -> {
                    List<Long> valores = bodegas.stream()
                            .map(b -> porBodega.getOrDefault(b, Map.of()).getOrDefault(estado, 0L)).toList();
                    return new PanelInventario.SerieEstado(estado.name(), estado.getEtiqueta(), valores,
                            valores.stream().mapToLong(Long::longValue).sum());
                })
                .toList();
        List<Long> totalPorBodega = bodegas.stream()
                .map(b -> porBodega.get(b).values().stream().mapToLong(Long::longValue).sum()).toList();
        long total = series.stream().mapToLong(PanelInventario.SerieEstado::total).sum();
        return new PanelInventario(bodegas, series, totalPorBodega, total, vendidos);
    }
}
