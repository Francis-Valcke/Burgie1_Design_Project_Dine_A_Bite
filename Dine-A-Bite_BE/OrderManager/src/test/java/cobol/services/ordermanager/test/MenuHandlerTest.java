package cobol.services.ordermanager.test;

import cobol.commons.MenuItem;
import cobol.commons.StandInfo;
import cobol.services.ordermanager.dbmenu.Food_Repository;
import cobol.services.ordermanager.dbmenu.StandRepository;
import cobol.services.ordermanager.dbmenu.StockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
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

import java.math.BigDecimal;
import java.math.MathContext;
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
    private ArrayList<BigDecimal> prices = new ArrayList<BigDecimal>(Arrays.asList(new BigDecimal[]{BigDecimal.valueOf(10.0), BigDecimal.valueOf(11.0), BigDecimal.valueOf(12.0), BigDecimal.valueOf(13.0), BigDecimal.valueOf(2.5), BigDecimal.valueOf(4.0)}));
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
        Map<String, StandInfo> standInfos = new HashMap<>();
        int n = 0;
        for (String key: stands.keySet()){
            JSONObject obj = new JSONObject();
            StandInfo si = new StandInfo(key, key.split("-")[0], (long)0.0+n*10, (long)0.0-n*10);
            standInfos.put(key, si);
            n++;
        }
        for (int i = 0;i<foodnames.size();i++){
             for (String key: stands.keySet()){
                if (stands.get(key).contains(i)){
                    List<String> cat = new ArrayList<>();
                    cat.add(categories.get(i));
                    MenuItem mi = new MenuItem(foodnames.get(i), prices.get(i), preptimes.get(i),20, key.split("-")[0], descriptions.get(i),cat);
                    standInfos.get(key).addMenuItem(mi);
                }
            }

        }

        for (String key: stands.keySet()){
            MvcResult result = this.mockMvc.perform(post("/addstand").contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token).content(objectMapper.writeValueAsString(standInfos.get(key)))).andReturn();
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
        JSONParser parser = new JSONParser();
        JSONArray menu = (JSONArray) parser.parse(json);
        for (int j=0;j<menu.size();j++){
            MenuItem mi = objectMapper.readValue(menu.get(j).toString(), MenuItem.class);
            int i = foodnames.indexOf(mi.getFoodName());
            for (String key: stands.keySet()){
                if (stands.get(key).contains(i)){
                    assertTrue(mi.getFoodName().equals(foodnames.get(i)));
                    assertTrue(mi.getPrice().round(new MathContext(2)).equals(prices.get(i).round(new MathContext(2))));
                    if (descriptions.get(i).equals(""))assertTrue(mi.getDescription()==null);
                    else assertTrue(descriptions.get(i).equals(mi.getDescription()));
                    boolean catOk = false;
                    for (String cat: mi.getCategory()){
                        if (cat.equals(categories.get(i))) catOk=true;
                    }
                    assertTrue(catOk);
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
