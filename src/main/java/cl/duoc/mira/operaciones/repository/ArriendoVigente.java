package cl.duoc.mira.operaciones.repository;

import java.time.LocalDate;

/** Equipo con un arriendo sin retorno registrado y la fecha en que debe volver. */
public record ArriendoVigente(Long equipoId, LocalDate fechaRetornoComprometida) {
}
