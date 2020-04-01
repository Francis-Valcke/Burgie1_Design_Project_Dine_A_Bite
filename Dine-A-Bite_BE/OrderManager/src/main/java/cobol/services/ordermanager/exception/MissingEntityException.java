package cobol.services.ordermanager.exception;

/**
 * Used when an object is not present in the database
 */
public class MissingEntityException extends Exception{

    public MissingEntityException(String errorMessage){
        super(errorMessage);
    }
}
