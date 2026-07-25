package exception;

public class ExchangeRatesServletException extends RuntimeException{
    public ExchangeRatesServletException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
