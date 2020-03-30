package cobol.services.dataset.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDatasetGenerator {

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext applicationContext;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.applicationContext)
                .build();
    }

    @Test
    public void load() throws Exception {

        //this.mockMvc
        //        .perform(
        //                get("/clear")
        //        )
        //        .andExpect(status().isOk());

        this.mockMvc
                .perform(
                        get("/load")
                )
                .andExpect(status().isOk());

    }
}
