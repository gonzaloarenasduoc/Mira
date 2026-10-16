package cl.duoc.mira.common.exception;

/**
 * El recurso no existe o pertenece a otra empresa. Ambos casos se responden igual para no
 * revelar datos de otra empresa (PNF-06).
 */
public class RecursoNoEncontradoException extends NegocioException {

    public RecursoNoEncontradoException(String recurso) {
        super(recurso + " no existe.");
    }
}
