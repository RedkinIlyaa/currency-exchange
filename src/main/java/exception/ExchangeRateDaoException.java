package exception;

public class ExchangeRateDaoException extends RuntimeException {

    public ExchangeRateDaoException(String message) {
        super(message);
    }

    public ExchangeRateDaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
