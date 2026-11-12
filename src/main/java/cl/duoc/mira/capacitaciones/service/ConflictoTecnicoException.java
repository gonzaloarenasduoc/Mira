package cl.duoc.mira.capacitaciones.service;

import cl.duoc.mira.common.exception.NegocioException;

/**
 * HU-21: el técnico ya tiene otra capacitación programada ese día. Es una advertencia, no un
 * impedimento: el vendedor puede confirmar y agendarla igual.
 */
public class ConflictoTecnicoException extends NegocioException {

    public ConflictoTecnicoException(String aviso) {
        super(aviso);
    }
}
