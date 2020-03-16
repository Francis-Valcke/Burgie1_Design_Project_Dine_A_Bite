package cobol.services.authentication.controller;

import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cobol.services.authentication.controller.ResponseModel.status.OK;

@AllArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {

    private UserRepository users;

    @GetMapping("")
    public ResponseEntity info(){

        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("Welcome to the admin page")
                        .build().generateResponse()
        );
    }



    @Autowired
    public void setUsers(UserRepository users) {
        this.users = users;
    }
}
