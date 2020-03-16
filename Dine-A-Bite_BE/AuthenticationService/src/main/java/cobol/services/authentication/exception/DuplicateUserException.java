package cobol.services.authentication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.ALREADY_REPORTED, reason = "The user already exists")
public class DuplicateUserException extends Throwable {

    public DuplicateUserException(String message) {
        super(message);
    }
}
