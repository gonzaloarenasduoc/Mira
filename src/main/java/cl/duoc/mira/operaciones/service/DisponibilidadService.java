package cl.duoc.mira.operaciones.service;

import cl.duoc.mira.inventario.dto.EquipoResponse;
import cl.duoc.mira.inventario.dto.FiltroEquipos;
import cl.duoc.mira.inventario.entity.EstadoEquipo;
import cl.duoc.mira.inventario.service.EquipoService;
import cl.duoc.mira.operaciones.dto.BodegaDisponibilidad;
import cl.duoc.mira.operaciones.dto.DisponibilidadResponse;
import cl.duoc.mira.operaciones.dto.FiltroDisponibilidad;
import cl.duoc.mira.operaciones.dto.UnidadDisponibilidad;
import cl.duoc.mira.operaciones.repository.ArriendoRepository;
import cl.duoc.mira.operaciones.repository.ArriendoVigente;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta de disponibilidad consolidada (E004). La disponibilidad no se almacena: un equipo está
 * disponible si su estado es DISPONIBLE y no tiene un arriendo vigente (03-modelo-datos.md, 1.6).
 * Los equipos vendidos no se muestran, porque ya salieron del inventario (CF-09).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisponibilidadService {

    private static final Comparator<UnidadDisponibilidad> ORDEN =
            Comparator.comparing((UnidadDisponibilidad u) -> !u.disponible())
                    .thenComparing(UnidadDisponibilidad::categoria)
                    .thenComparing(UnidadDisponibilidad::numeroSerie);

    private final EquipoService equipoService;
    private final ArriendoRepository arriendoRepository;
    private final Clock clock;

    public DisponibilidadResponse consultar(Long empresaId, FiltroDisponibilidad filtro) {
        LocalDate hoy = LocalDate.now(clock);
        FiltroEquipos criterios = new FiltroEquipos();
        criterios.setTexto(filtro.getTexto());
        criterios.setCategoriaId(filtro.getCategoriaId());
        List<EquipoResponse> equipos = equipoService.buscar(empresaId, criterios).stream()
                .filter(e -> e.estado() != EstadoEquipo.VENDIDO)
                .toList();
        Map<Long, LocalDate> retornos = arriendoRepository.vigentes(empresaId).stream()
                .collect(Collectors.toMap(ArriendoVigente::equipoId, ArriendoVigente::fechaRetornoComprometida,
                        (a, b) -> a.isBefore(b) ? a : b));

        Map<String, List<UnidadDisponibilidad>> porBodega = equipos.stream()
                .collect(Collectors.groupingBy(EquipoResponse::bodega, TreeMap::new,
                        Collectors.mapping(e -> aUnidad(e, retornos.get(e.id()), hoy), Collectors.toList())));
        List<BodegaDisponibilidad> bodegas = porBodega.entrySet().stream()
                .map(g -> {
                    List<UnidadDisponibilidad> unidades = g.getValue().stream().sorted(ORDEN).toList();
                    return new BodegaDisponibilidad(g.getKey(),
                            unidades.stream().filter(UnidadDisponibilidad::disponible).count(), unidades);
                })
                .toList();
        long disponibles = bodegas.stream().mapToLong(BodegaDisponibilidad::disponibles).sum();
        return new DisponibilidadResponse(bodegas, disponibles, equipos.size(), hoy);
    }

    private static UnidadDisponibilidad aUnidad(EquipoResponse e, LocalDate fechaRetorno, LocalDate hoy) {
        boolean disponible = e.estado() == EstadoEquipo.DISPONIBLE && fechaRetorno == null;
        long atraso = fechaRetorno != null && fechaRetorno.isBefore(hoy)
                ? ChronoUnit.DAYS.between(fechaRetorno, hoy) : 0;
        return new UnidadDisponibilidad(e.id(), e.numeroSerie(), e.categoria(), e.marca(), e.modelo(),
                e.estado(), disponible, fechaRetorno, atraso);
    }
}
