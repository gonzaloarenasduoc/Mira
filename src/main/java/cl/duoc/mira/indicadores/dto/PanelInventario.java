package cl.duoc.mira.indicadores.dto;

import java.util.List;

/**
 * Equipos por bodega y por estado para los gráficos del panel (HU-26). {@code series} trae un estado
 * por elemento, con su cantidad en cada bodega en el mismo orden que {@code bodegas}. Los vendidos no
 * son parte del inventario y se informan aparte (D23).
 */
public record PanelInventario(List<String> bodegas, List<SerieEstado> series, List<Long> totalPorBodega,
                              long total, long vendidos) {

    public record SerieEstado(String estado, String etiqueta, List<Long> porBodega, long total) {
    }
}
