package exception;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String message, Throwable e) {
        super(message, e);
    }
}
