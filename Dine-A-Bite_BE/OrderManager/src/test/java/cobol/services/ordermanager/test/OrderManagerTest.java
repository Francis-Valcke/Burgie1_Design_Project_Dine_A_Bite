package cobol.services.ordermanager.test;




import cobol.services.ordermanager.Order;
import cobol.services.ordermanager.OrderProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderManagerTest {

    private class OrderEntry {
        public Map<String, Double> location = new HashMap<>();
        public Map<String, Integer> order = new HashMap<>();

        OrderEntry() {
            location.put("latitude", 37.421998);
            location.put("longitude", 122.084);
            order.put("Nice pizza", 4);
        }

    }

    private MockMvc mockMvc;

    private OrderProcessor orderProcessor = OrderProcessor.getOrderProcessor();

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
    public void pingTest() throws Exception {
        this.mockMvc
                .perform(
                    get("/pingOM")
                )
                .andExpect(status().isOk());
    }

    @Test
    public void placeOrderTest() throws Exception {
        OrderEntry entry = new OrderEntry();
        JSONObject response = new JSONObject(this.mockMvc
                .perform(
                    post("/placeOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(entry))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        int id = response.getInt("order_id");
        assert (id == 1);
        assert (orderProcessor.getOrder(1).getClass() == Order.class);
        assert (orderProcessor.getOrder(2) == null);
    }

    /*@Test
    *
    * TODO: This test is fails, unless the eventchannel is running. A solution for starting up the event service is required
    *
    *
    *
    public void testConfirmStand() throws Exception {
        this.mockMvc.perform(
                get("/confirmStand")
                .param("order_id", "1")
                .param("stand_id", "1")
        )
        .andExpect(status().isOk());
    }*/
}
