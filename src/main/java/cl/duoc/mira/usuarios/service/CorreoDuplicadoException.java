package cl.duoc.mira.usuarios.service;

import cl.duoc.mira.common.exception.NegocioException;

public class CorreoDuplicadoException extends NegocioException {

    public CorreoDuplicadoException(String email) {
        super("Ya existe un usuario con el correo " + email + ".");
    }
}
