package cobol.services.authentication.test;

import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext applicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository users;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Before
    public void saveTestUsers() {
        users.saveAndFlush(
                User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .role(Arrays.asList("ROLE_USER", "ROLE_ADMIN"))
                        .build()
        );

        users.saveAndFlush(
                User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("user"))
                        .role(Arrays.asList("ROLE_USER"))
                        .build()
        );
    }

    @Test
    public void pingTest() throws Exception {
        this.mockMvc
                .perform(
                        get("/pingAS")
                                //.accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    public void getUserInfoWithoutAuthenticationTest() throws Exception {
        this.mockMvc
                .perform(
                        get("/user")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void performAuthenticateTestUserTest() throws Exception {
        authenticateTestUser();
    }

    @Test
    public void performAuthenticateTestAdminTest() throws Exception {
        authenticateTestAdmin();
    }

    @Test
    public void userRoleAuthenticationTest() throws Exception {
        String token = authenticateTestUser();
        this.mockMvc
                .perform(
                        get("/user")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());

        this.mockMvc
                .perform(
                        get("/admin")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void userAdminAuthenticationTest() throws Exception {
        String token = authenticateTestAdmin();
        this.mockMvc
                .perform(
                        get("/user")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());

        this.mockMvc
                .perform(
                        get("/admin")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());
    }

    @Test
    public void createUserTest() throws Exception {
        this.mockMvc
                .perform(
                        post("/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(
                                        User.builder().username("test").password("test").build()
                                ))
                )
                .andExpect(status().isOk());
    }

    @After
    public void removeTestUsers() throws Exception {
        users.deleteAll(Arrays.asList(
                users.findById("admin").get(),
                users.findById("user").get()
        ));
    }


    private String authenticateTestUser() throws Exception {
        JSONObject response = new JSONObject(this.mockMvc
                .perform(
                        post("/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(
                                        User.builder().username("user").password("user").build()
                                ))
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()
        );
        return (String) response.getJSONObject("details").get("token");
    }


    private String authenticateTestAdmin() throws Exception {
        JSONObject response = new JSONObject(this.mockMvc
                .perform(
                        post("/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(
                                        User.builder().username("admin").password("admin").build()
                                ))
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()
        );
        return (String) response.getJSONObject("details").get("token");
    }





}
