package exception.exist;

public class CurrencyAlreadyExistsException extends AlreadyExistsException {
    public CurrencyAlreadyExistsException(String message, Throwable e) {
        super(message, e);
    }
}
