package cobol.services.ordermanager;

import cobol.commons.ResponseModel;
import cobol.commons.security.jwt.JwtVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cobol.commons.ResponseModel.status.OK;
import static org.springframework.http.ResponseEntity.ok;

@RestController
public class OrderManagerController {

    /**
     * API endpoint to test if the server is still alive.
     *
     * @return "OrderManager is alive!"
     */
    @GetMapping("/ping")
    public ResponseEntity ping() {
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("OrderManager is alive!")
                        .build().generateResponse()
        );
    }

}
