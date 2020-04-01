package cobol.services.ordermanager.exception;

/**
 * Used when a value has to be set, but is already configured
 * For example when a confirmStand request tries to confirm a stand twice (the value can not be overwritten)
 */
public class AlreadySetException extends Exception {

    public AlreadySetException (String errorMessage){
        super(errorMessage);
    }
}
