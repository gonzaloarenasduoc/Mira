package cl.duoc.mira.operaciones.repository;

import java.time.LocalDate;

/** Línea de arriendo sin retorno registrado, con su cliente y su fecha comprometida (HU-16). */
public record LineaPorRetornar(Long arriendoId, String cliente, Long equipoId, LocalDate fechaRetornoComprometida) {
}
