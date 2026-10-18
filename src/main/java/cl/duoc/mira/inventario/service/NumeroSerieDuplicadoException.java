package cl.duoc.mira.inventario.service;

import cl.duoc.mira.common.exception.NegocioException;

public class NumeroSerieDuplicadoException extends NegocioException {

    public NumeroSerieDuplicadoException(String numeroSerie) {
        super("Ya existe un equipo con el número de serie " + numeroSerie + " en la empresa.");
    }
}
