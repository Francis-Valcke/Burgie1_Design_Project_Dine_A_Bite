package cobol.services.authentication;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {
    @RequestMapping("/pingAS")
    public String index() {
        return "Response from Authentication Service";
    }
}
