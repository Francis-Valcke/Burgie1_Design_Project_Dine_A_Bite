package cobol.services.authentication.test;

import cobol.commons.communication.response.BetterResponseModel;
import cobol.services.authentication.domain.entity.User;
import cobol.services.authentication.domain.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONObject;
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

import static cobol.commons.stub.AuthenticationServiceStub.*;
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
        objectMapper.registerModule(new JavaTimeModule());
        this.mockMvc = webAppContextSetup(this.applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void pingTest() throws Exception {
        this.mockMvc
                .perform(
                        get(GET_PING)
                )
                .andExpect(status().isOk());
    }

    @Test
    public void getUserInfoWithoutAuthenticationTest() throws Exception {
        this.mockMvc
                .perform(
                        get(GET_USER)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void createUserTest() throws Exception {
        createUser("test");
    }

    @Test
    public void authenticateUserTest() throws Throwable {
        createUser("test");
        authenticateUser("test");
    }

    @Test
    public void userRoleAuthenticationTest() throws Throwable {
        String token = authenticateUser("user");
        this.mockMvc
                .perform(
                        get(GET_USER)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());

        this.mockMvc
                .perform(
                        get(GET_ADMIN_INFO)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void adminRoleAuthenticationTest() throws Throwable {
        String token = authenticateUser("admin");
        this.mockMvc
                .perform(
                        get(GET_USER)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());

        this.mockMvc
                .perform(
                        get(GET_ADMIN_INFO)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());
    }

    private void createUser(String user) throws Exception {
        this.mockMvc
                .perform(
                        post(POST_CREATE_USER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(
                                        User.builder().username(user).password(user+user).build()
                                ))
                )
                .andExpect(status().isOk());
    }

    private String authenticateUser(String user) throws Throwable {
        String response = this.mockMvc
                .perform(
                        post(POST_AUTHENTICATE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(
                                        User.builder().username(user).password(user+user).build()
                                ))
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(response, new TypeReference<BetterResponseModel<String>>() {}).getOrThrow();
    }

}
