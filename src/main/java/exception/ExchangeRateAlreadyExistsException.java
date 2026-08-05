package exception;

public class ExchangeRateAlreadyExistsException extends AlreadyExistsException {
    public ExchangeRateAlreadyExistsException(String message, Throwable t) {
        super(message, t);
    }
}
