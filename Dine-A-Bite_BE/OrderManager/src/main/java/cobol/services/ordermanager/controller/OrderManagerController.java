package cobol.services.ordermanager.controller;

import cobol.commons.BetterResponseModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderManagerController {

    /**
     * This API will test if the server is still alive.
     *
     * @return "OrderManager is alive!"
     */
    @GetMapping("/pingOM")
    public ResponseEntity<BetterResponseModel<?>> ping() {
        return ResponseEntity.ok(BetterResponseModel.ok("OrderManager is alive", null));
    }

}
