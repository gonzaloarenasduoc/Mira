package cl.duoc.mira.inventario.service;

import cl.duoc.mira.inventario.dto.CategoriaResponse;
import cl.duoc.mira.inventario.repository.CategoriaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaResponse> listarActivas(Long empresaId) {
        return categoriaRepository.findByEmpresaIdAndActivaTrueOrderByNombre(empresaId).stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNombre(), c.isRequiereCapacitacion()))
                .toList();
    }
}
