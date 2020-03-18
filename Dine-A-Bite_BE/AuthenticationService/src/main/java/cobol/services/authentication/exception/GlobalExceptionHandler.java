package cobol.services.authentication.exception;

import cobol.commons.security.exception.DuplicateUserException;
import cobol.commons.ResponseModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import static cobol.commons.ResponseModel.status.ERROR;

/**
 * When exceptions are thrown by rest controllers they will be routed here when they are handles within this class.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({AuthenticationException.class, UsernameNotFoundException.class})
    public ResponseEntity handleAuthenticationException(HttpServletRequest request, Exception ex){
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(ERROR.toString())
                        .details(ex.getMessage())
                        .build().generateResponse()
        );
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity handleDuplicateUserException(HttpServletRequest request, Exception ex){
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(ERROR.toString())
                        .details(ex.getCause().getMessage())
                        .build().generateResponse()
        );
    }

}
