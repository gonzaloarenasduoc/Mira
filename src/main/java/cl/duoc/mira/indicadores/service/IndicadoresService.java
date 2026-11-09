package cl.duoc.mira.indicadores.service;

import cl.duoc.mira.operaciones.dto.AlertaRetornoResponse;
import cl.duoc.mira.operaciones.service.ArriendoService;
import java.util.List;
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

    private final ArriendoService arriendoService;

    /**
     * Equipos con retorno vencido, del más atrasado al menos atrasado (HU-28, CF-10). Reutiliza las
     * alertas de HU-16 y deja fuera los que todavía no vencen.
     */
    public List<AlertaRetornoResponse> retornosVencidos(Long empresaId) {
        return arriendoService.alertasDeRetorno(empresaId).stream()
                .filter(AlertaRetornoResponse::vencido)
                .toList();
    }
}
