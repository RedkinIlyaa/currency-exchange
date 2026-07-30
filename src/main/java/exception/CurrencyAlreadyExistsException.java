package exception;

public class CurrencyAlreadyExistsException extends RuntimeException {
    public CurrencyAlreadyExistsException(String message, Throwable e) {
        super(message, e);
    }
}
