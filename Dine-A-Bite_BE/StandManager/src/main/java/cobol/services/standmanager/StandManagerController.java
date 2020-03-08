package cobol.services.standmanager;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StandManagerController {
    @RequestMapping("/pingSM")
    public String index() {
        return "Response from Stand Manager";
    }
}
