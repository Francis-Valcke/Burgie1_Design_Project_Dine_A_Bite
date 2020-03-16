package cobol.services.authentication.controller;

import cobol.services.authentication.exception.DuplicateUserException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import static cobol.services.authentication.controller.ResponseModel.status.ERROR;

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
