package cobol.services.authentication.controller;

import cobol.commons.ResponseModel;
import cobol.commons.stub.AuthenticationServiceStub;
import cobol.services.authentication.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cobol.commons.ResponseModel.status.OK;

/**
 * REST api controller what requires ROLE_ADMIN for access.
 */
@AllArgsConstructor
@RestController
public class AdminController {


    /**
     * Temporary API endpoint for testing role security.
     *
     * @return ResponseEntity
     */
    @GetMapping(AuthenticationServiceStub.GET_ADMIN_INFO)
    public ResponseEntity info(){

        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("Welcome to the admin page")
                        .build().generateResponse()
        );
    }

}
