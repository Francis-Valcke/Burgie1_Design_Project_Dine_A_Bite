package cobol.services.authentication.controller;

import cobol.services.authentication.exception.InvalidJwtAuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static cobol.services.authentication.controller.ResponseModel.status.OK;

@ControllerAdvice
public class RestEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ InvalidJwtAuthenticationException.class, AuthenticationException.class })
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ResponseEntity handleAuthenticationException(Exception e) {
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details(e.getLocalizedMessage())
                        .build().generateResponse()
        );
    }
}
