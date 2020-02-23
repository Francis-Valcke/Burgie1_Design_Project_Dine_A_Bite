package cobol.services.authentication;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderManagerController {
    @RequestMapping("/pingOM")
    public String index() {
        return "Response from Order Manager";
    }
}
