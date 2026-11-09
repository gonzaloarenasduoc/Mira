package cl.duoc.mira.indicadores.service;

import cl.duoc.mira.common.exception.NegocioException;
import cl.duoc.mira.indicadores.dto.PanelInventario;
import cl.duoc.mira.indicadores.dto.UtilizacionResponse;
import cl.duoc.mira.inventario.dto.ConteoEquipos;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.AlertaRetornoResponse;
import cl.duoc.mira.operaciones.dto.PeriodoArrendado;
import cl.duoc.mira.operaciones.service.ArriendoService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

    /** Equipos que se muestran en cada tabla de más y menos usados (HU-27). */
    static final int EQUIPOS_POR_TABLA = 10;

    private final ArriendoService arriendoService;
    private final EquipoService equipoService;
    private final Clock clock;

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

    /** HU-27: todos los equipos arrendados sin retorno, con su fecha comprometida. */
    public List<AlertaRetornoResponse> arriendosVigentes(Long empresaId) {
        return arriendoService.arriendosVigentes(empresaId);
    }

    /**
     * Utilización de los equipos entre {@code desde} y {@code hasta}, ambos incluidos (HU-27, D24).
     * Sin fechas, los últimos 30 días. Cuenta como arrendado cada día desde el inicio del arriendo
     * hasta el anterior al retorno, como el cobro (D18); si el equipo sigue fuera, hasta hoy. Los días
     * disponibles de un equipo empiezan en su ingreso si este cae dentro del periodo.
     */
    public UtilizacionResponse utilizacion(Long empresaId, LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now(clock);
        LocalDate fin = hasta == null ? hoy : hasta;
        LocalDate inicio = desde == null ? fin.minusDays(29) : desde;
        if (inicio.isAfter(fin)) {
            throw new NegocioException("La fecha de inicio del periodo no puede ser posterior a la de término.");
        }
        if (fin.isAfter(hoy)) {
            throw new NegocioException("El periodo no puede terminar en una fecha futura.");
        }

        Map<Long, List<PeriodoArrendado>> tramos = arriendoService.arrendadosEntre(empresaId, inicio, fin).stream()
                .collect(Collectors.groupingBy(PeriodoArrendado::equipoId));
        List<UtilizacionResponse.FilaEquipo> equipos = equipoService.enInventario(empresaId).stream()
                .filter(e -> !e.ingreso().isAfter(fin))
                .map(e -> {
                    LocalDate disponibleDesde = e.ingreso().isAfter(inicio) ? e.ingreso() : inicio;
                    long disponibles = ChronoUnit.DAYS.between(disponibleDesde, fin) + 1;
                    long arrendado = Math.min(disponibles, tramos.getOrDefault(e.id(), List.of()).stream()
                            .mapToLong(t -> diasArrendado(t, disponibleDesde, fin, hoy)).sum());
                    return new UtilizacionResponse.FilaEquipo(e.id(), e.numeroSerie(), e.equipo(), e.categoria(),
                            arrendado, disponibles, porcentaje(arrendado, disponibles));
                })
                .sorted(Comparator.comparingDouble(UtilizacionResponse.FilaEquipo::porcentaje).reversed()
                        .thenComparing(UtilizacionResponse.FilaEquipo::numeroSerie))
                .toList();

        List<UtilizacionResponse.Fila> porCategoria = equipos.stream()
                .collect(Collectors.groupingBy(UtilizacionResponse.FilaEquipo::categoria, TreeMap::new, Collectors.toList()))
                .entrySet().stream().map(c -> fila(c.getKey(), c.getValue())).toList();
        int mas = Math.min(EQUIPOS_POR_TABLA, equipos.size());
        int menos = Math.min(EQUIPOS_POR_TABLA, equipos.size() - mas);
        List<UtilizacionResponse.FilaEquipo> menosUsados = new ArrayList<>(equipos.subList(equipos.size() - menos, equipos.size()));
        Collections.reverse(menosUsados);
        return new UtilizacionResponse(inicio, fin, ChronoUnit.DAYS.between(inicio, fin) + 1, fila("Todos los equipos", equipos),
                porCategoria, equipos.subList(0, mas), menosUsados);
    }

    /** Días del tramo que caen entre {@code desde} y {@code hasta}; el día del retorno no cuenta. */
    static long diasArrendado(PeriodoArrendado t, LocalDate desde, LocalDate hasta, LocalDate hoy) {
        LocalDate finExclusivo = t.retornoReal() == null ? hoy.plusDays(1)
                : t.retornoReal().equals(t.inicio()) ? t.inicio().plusDays(1) : t.retornoReal();
        LocalDate a = t.inicio().isAfter(desde) ? t.inicio() : desde;
        LocalDate b = finExclusivo.isBefore(hasta.plusDays(1)) ? finExclusivo : hasta.plusDays(1);
        return Math.max(0, ChronoUnit.DAYS.between(a, b));
    }

    private static UtilizacionResponse.Fila fila(String nombre, List<UtilizacionResponse.FilaEquipo> equipos) {
        long arrendado = equipos.stream().mapToLong(UtilizacionResponse.FilaEquipo::diasArrendado).sum();
        long disponibles = equipos.stream().mapToLong(UtilizacionResponse.FilaEquipo::diasDisponibles).sum();
        return new UtilizacionResponse.Fila(nombre, equipos.size(), arrendado, disponibles, porcentaje(arrendado, disponibles));
    }

    private static double porcentaje(long arrendado, long disponibles) {
        return disponibles == 0 ? 0 : Math.round(arrendado * 1000.0 / disponibles) / 10.0;
    }
}
