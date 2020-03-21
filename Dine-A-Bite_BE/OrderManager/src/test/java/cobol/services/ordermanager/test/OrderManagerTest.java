package cobol.services.ordermanager.test;



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
public class OrderManagerTest {

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext applicationContext;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void pingTest() throws Exception {
        this.mockMvc
                .perform(
                    get("/pingOM")
                )
                .andExpect(status().isOk());
    }

    @Test
    public void placeOrderTest() throws Exception {

    }
}
