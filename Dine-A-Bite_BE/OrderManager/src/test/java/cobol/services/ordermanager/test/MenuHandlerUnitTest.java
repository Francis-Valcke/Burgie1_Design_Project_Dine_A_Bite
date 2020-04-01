package cobol.services.ordermanager.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MenuHandlerUnitTest {

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext applicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.applicationContext)
                .apply(springSecurity())
                .build();
    }


    @Test
    public void deleteAll() {
    }

    @Test
    public void refreshCache() {
    }

    @Test
    public void updateStandManager() {
    }

    @Test
    public void getStands() {
    }

    @Test
    public void getStandMenu() {
    }

    @Test
    public void getStand() {
    }

    @Test
    public void updateGlobalMenu() {
    }

    @Test
    public void updateStand() {
    }

    @Test
    public void addStand() {
    }

    @Test
    public void deleteStand() {
    }

    @Test
    public void sendRestCallToStandManager() {
    }

    @Test
    public void distinctByKey() {
    }

    @Test
    public void getGlobalMenu() {
    }
}
