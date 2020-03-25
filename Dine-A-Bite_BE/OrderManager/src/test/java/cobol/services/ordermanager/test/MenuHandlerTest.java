package cobol.services.ordermanager.test;

import cobol.services.ordermanager.dbmenu.Food_Repository;
import cobol.services.ordermanager.dbmenu.StandRepository;
import cobol.services.ordermanager.dbmenu.StockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import cobol.services.ordermanager.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Testset:
 * 2 stands of same brand
 * 1 stand of different brand
 * Every stand has unique items and items they share with other stands
 * 
 * Tests in this class:
 * Stands correctly added - check
 * Menu items correctly added:
 * 1. in global menu: items with same name from different brands appear once for every brand - check
 * 2. in stand menu: all items appear from requested stand and none from other stands -TODO
 * Menu items correcty changed - TODO
 * Stands correctly deleted - check
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MenuHandlerTest {
    private MockMvc mockMvc;
    private String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmcmFuY2lzIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYXQiOjE1ODQ2MTAwMTcsImV4cCI6MTc0MjI5MDAxN30.5UNYM5Qtc4anyHrJXIuK0OUlsbAPNyS9_vr-1QcOWnQ";
    private ArrayList<String> foodnames = new ArrayList<String>(Arrays.asList(new String[]{"burger1", "burger2", "pizza1", "pizza2", "cola", "fries"}));
    private Map<String, List<Integer>> stands = new HashMap<String, List<Integer>>() {{
        put("burgerstand-1", Arrays.asList(new Integer[]{0, 4, 5}));
        put("burgerstand-2", Arrays.asList(new Integer[]{1, 4, 5}));
        put("pizzastand-1", Arrays.asList(new Integer[]{2, 3, 4}));
    }};
    private ArrayList<Double> prices = new ArrayList<Double>(Arrays.asList(new Double[]{10.0, 11.0, 12.0, 13.0, 2.5, 4.0}));
    private ArrayList<Integer> preptimes = new ArrayList<Integer>(Arrays.asList(new Integer[]{10, 11, 12, 13, 2, 4}));
    private ArrayList<String> categories = new ArrayList<String>(Arrays.asList(new String[]{"burger", "burger", "pizza", "pizza", "drink", "fries"}));
    private ArrayList<String> descriptions = new ArrayList<String>(Arrays.asList(new String[]{"burger with bacon", "burger with cheese", "pizza with salami", "pizza with pineapple", "", ""}));


    @Autowired
    WebApplicationContext applicationContext;

    @Autowired
    ObjectMapper objectMapper;


    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(this.applicationContext)
                .apply(springSecurity())
                .build();
        this.mockMvc.perform(get("/SMswitch?on=false").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token));
        Map<String, JSONObject> objs = new HashMap<>();
        ArrayList<Object> o = null;
        int n = 0;
        for (String key: stands.keySet()){
            JSONObject obj = new JSONObject();
            o = new ArrayList<Object>();
            o.add(key.split("-")[0]);
            o.add(0.0+n*10);
            o.add(0.0-n*10);
            obj.put(key, o);
            objs.put(key, obj);
        }

        ArrayList<Object> o2 = null;
        for (int i = 0;i<foodnames.size();i++){
            o2 = new ArrayList<Object>();
            o2.add(prices.get(i));
            o2.add(preptimes.get(i));
            o2.add(20);
            o2.add(categories.get(i));
            o2.add(descriptions.get(i));
            for (String key: stands.keySet()){
                if (stands.get(key).contains(i)){
                    objs.get(key).put(foodnames.get(i),o2);
                }
            }

        }

        for (String key: stands.keySet()){
            MvcResult result = this.mockMvc.perform(post("/addstand").contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token).content(String.valueOf(objs.get(key)))).andReturn();
            String ret=result.getResponse().getContentAsString();
            assertTrue(ret.equals("Saved"));
        }


    }


    @Test
    public void getMenuTest() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/menu").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token))
                .andReturn();
        String json=result.getResponse().getContentAsString();
        System.out.println(json);
        JSONParser parser = new JSONParser();
        JSONObject menu = (JSONObject) parser.parse(json);
        for (int i=0;i<foodnames.size();i++){
            for (String key: stands.keySet()){
                if (stands.get(key).contains(i)){
                    assertTrue(menu.keySet().contains(foodnames.get(i)+"_"+key.split("-")[0]));
                    ArrayList a = (ArrayList) menu.get(foodnames.get(i)+"_"+key.split("-")[0]);
                    assertTrue((double)a.get(0)==prices.get(i));
                    if (descriptions.get(i)=="")assertTrue(((String)a.get(2))==null);
                    else assertTrue(((String)a.get(2)).equals(descriptions.get(i)));
                    assertTrue(((ArrayList<String>)a.get(1)).get(0).equals(categories.get(i)));

                }


            }
        }

    }
    @After
    public void clearMenus() throws Exception {
        for (String key: stands.keySet()){
            MvcResult result = this.mockMvc.perform(get(String.format("/deleteStand?standname=%s", key)).contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token)).andReturn();
            String ret1=result.getResponse().getContentAsString();
            assertTrue(ret1.equals("Stand "+key+" deleted."));
        }
    }


}
