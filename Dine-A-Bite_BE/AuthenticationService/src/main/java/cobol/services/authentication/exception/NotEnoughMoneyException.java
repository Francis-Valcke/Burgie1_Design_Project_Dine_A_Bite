package cobol.services.authentication.exception;

public class NotEnoughMoneyException extends Throwable {
    public NotEnoughMoneyException() {

    }

    public NotEnoughMoneyException(String message) {
        super(message);
    }
}
