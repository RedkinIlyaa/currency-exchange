package exception;

public class CurrencyDaoException extends RuntimeException {

    public CurrencyDaoException(String message) {
        super(message);
    }

    public CurrencyDaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
