package cobol.services.ordermanager.exception;

/**
 * Used when an non-persistent object is not present in an array/map belonging to an Spring service
 */
public class MissingRunException extends Exception {

    public MissingRunException (String errorMessage){
        super(errorMessage);
    }
}
