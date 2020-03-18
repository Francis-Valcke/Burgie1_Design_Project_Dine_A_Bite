package cobol.services.ordermanager;

import cobol.commons.ResponseModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static cobol.commons.ResponseModel.status.OK;

@RestController
public class OrderManagerController {

    /**
     * API endpoint to test if the server is still alive.
     *
     * @return "OrderManager is alive!"
     */
    @GetMapping("/pingOM")
    public ResponseEntity ping() {
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("OrderManager is alive!")
                        .build().generateResponse()
        );
    }

}
