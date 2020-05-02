package cobol.services.ordermanager.exception;

public class NotEnoughMoneyException extends Throwable {
    public NotEnoughMoneyException() {

    }

    public NotEnoughMoneyException(String message) {
        super(message);
    }
}
