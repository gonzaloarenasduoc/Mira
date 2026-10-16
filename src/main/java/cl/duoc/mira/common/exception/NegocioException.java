package cl.duoc.mira.common.exception;

/**
 * Regla de negocio incumplida. El mensaje se muestra tal cual al usuario, por lo que se
 * escribe en el lenguaje del negocio y nunca contiene detalles técnicos (PNF-10).
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
