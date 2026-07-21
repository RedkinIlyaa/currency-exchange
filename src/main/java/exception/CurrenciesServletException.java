package exception;

public class CurrenciesServletException extends RuntimeException {

    public CurrenciesServletException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
