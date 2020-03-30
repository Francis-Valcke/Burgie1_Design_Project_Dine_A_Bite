package cobol.services.eventchannel.test;


import cobol.commons.Event;
import cobol.services.eventchannel.EventBroker;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EventChannelTest {

    private static boolean setupDone = false;

    private List<Event> eventList = new ArrayList<>();

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext applicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setup() throws Exception {
        EventBroker broker = EventBroker.getInstance();
        Thread brokerThread = new Thread(broker);
        brokerThread.start();
        this.mockMvc = webAppContextSetup(this.applicationContext)
                .build();
        if (!setupDone) {
            this.mockMvc
                    .perform(
                            get("/registerSubscriber")
                                    .param("types", "1,2,3,5")
                    );
            JSONObject testData = new JSONObject();
            testData.put("data", "This is a test");
            List<String> typesOne = new ArrayList<>();
            typesOne.add("1");
            typesOne.add("2");
            Event eventOne = new Event(testData, typesOne, "test");
            List<String> typesTwo = new ArrayList<>();
            typesTwo.add("3");
            typesTwo.add("4");
            Event eventTwo = new Event(testData, typesTwo, "test");
            eventList.add(eventOne);
            eventList.add(eventTwo);
            for (Event e : eventList) {
                this.mockMvc
                        .perform(
                                post("/publishEvent")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsBytes(e))
                        )
                        .andExpect(status().isOk());
            }
        }
        setupDone = true;
    }

    @Test
    public void pingTest() throws Exception {
        this.mockMvc
                .perform(
                        get("/pingEC")
                )
                .andExpect(status().isOk());
    }

    @Test
    public void subscribeTest() throws Exception {
        String id = this.mockMvc
                .perform(
                        get("/registerSubscriber")
                                .param("types", "1,2,3,5")
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assert (id.equals("1"));
    }

    @Test
    public void addChannelsTest() throws Exception {
        this.mockMvc
                .perform(
                        get("/registerSubscriber/toChannel")
                                .param("type", "4")
                                .param("id", "0")
                )
                .andExpect(status().isOk());
    }

    @Test
    public void deRegisterTest() throws Exception {
        this.mockMvc
                .perform(
                        get("/deregisterSubscriber")
                                .param("id", "0")
                                .param("type", "5")
                )
                .andExpect(status().isOk());
    }
    @Test
    public void testEventPolling() throws Exception {
        String data = this.mockMvc
                .perform(
                        get("/events")
                                .param("id", "0")
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JSONObject response = objectMapper.readValue(data, JSONObject.class);
        String details = (String) response.get("details");
        List<Event> eventList = objectMapper.readValue(details, new TypeReference<List<Event>>() {
        });
        assert (eventList.size() == 3); //event from channel 1,2 and 3;
        for (Event e : eventList) {
            assert (e.getMyId() == 0 || e.getMyId() == 1);
        }
    }
}
