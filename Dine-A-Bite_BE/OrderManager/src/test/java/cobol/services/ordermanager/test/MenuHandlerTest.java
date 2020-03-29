package cobol.services.ordermanager.test;

import cobol.commons.MenuItem;
import cobol.commons.StandInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Testset:
 * 2 stands of same brand
 * 1 stand of different brand
 * Every stand has unique items and items they share with other stands
 * All values vary from normal Strings and numbers (no size limits) to negative numbers and empty strings
 * Tests in this class:
 * Stands correctly added - check
 * Menu items correctly added:
 * 1. in global menu: items with same name from different brands appear once for every brand - check
 * 2. in stand menu: all items appear from requested stand and none from other stands -check
 * Menu items correcty changed:
 * 1. if new value is -1 for price or preptime, or "" for categories or description, then the old value is not changed -check
 * 2. if category has changed, it is added to list- check
 * 3. new items added
 * 4. items deleted if not present in new menu
 * Stands correctly deleted - check
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MenuHandlerTest {
    @Autowired
    WebApplicationContext applicationContext;
    @Autowired
    ObjectMapper objectMapper;
    private MockMvc mockMvc;
    private String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmcmFuY2lzIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYXQiOjE1ODQ2MTAwMTcsImV4cCI6MTc0MjI5MDAxN30.5UNYM5Qtc4anyHrJXIuK0OUlsbAPNyS9_vr-1QcOWnQ";
    private ArrayList<String> foodnames = new ArrayList<String>(Arrays.asList("burger1", "burger2", "pizza1", "pizza2", "cola", "fries"));
    private Map<String, List<Integer>> stands = new HashMap<String, List<Integer>>() {{
        put("burgerstand-1", Arrays.asList(0, 4, 5));
        put("burgerstand-2", Arrays.asList(1, 4, 5));
        put("pizzastand-1", Arrays.asList(2, 3, 4));
    }};
    private ArrayList<BigDecimal> prices = new ArrayList<BigDecimal>(Arrays.asList(BigDecimal.valueOf(10.0), BigDecimal.valueOf(11.0), BigDecimal.valueOf(12.0), BigDecimal.valueOf(13.0), BigDecimal.valueOf(2.5), BigDecimal.valueOf(4.0)));
    private ArrayList<Integer> preptimes = new ArrayList<Integer>(Arrays.asList(10, 11, 12, 13, 2, 4));
    private ArrayList<String> categories = new ArrayList<String>(Arrays.asList("burger", "", "pizza", "pizza", "drink", "fries"));
    private ArrayList<String> descriptions = new ArrayList<String>(Arrays.asList("burger with bacon", "burger with cheese", "pizza with salami", "pizza with pineapple", "", "with frietsauce"));
    private String time;
    /**
     * setup stands for testing
     *
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {
        //setup mockmvc of OrderManager application
        this.mockMvc = webAppContextSetup(this.applicationContext)
                .apply(springSecurity())
                .build();
        //disable sending stands to Stand Manager for these tests
        this.mockMvc.perform(get("/SMswitch?on=false").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token));
        //create a unique
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        time=timestamp.toString();
        Map<String, StandInfo> standInfos = new HashMap<>();
        int n = 0;
        for (String key : stands.keySet()) {
            StandInfo si = new StandInfo(key.concat(time), key.split("-")[0].concat(time), (long) n * 10, (long) -n * 10);
            standInfos.put(key, si);
            n++;
        }
        for (int i = 0; i < foodnames.size(); i++) {
            for (String key : stands.keySet()) {
                if (stands.get(key).contains(i)) {
                    List<String> cat = new ArrayList<>();
                    cat.add(categories.get(i));
                    MenuItem mi = new MenuItem(foodnames.get(i), prices.get(i), preptimes.get(i), 20, key.split("-")[0], descriptions.get(i), cat);
                    standInfos.get(key).addMenuItem(mi);
                }
            }

        }

        for (String key : stands.keySet()) {
            MvcResult result = this.mockMvc.perform(post("/addstand").contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token).content(objectMapper.writeValueAsString(standInfos.get(key)))).andReturn();
            String ret = result.getResponse().getContentAsString();
            assertEquals("Saved", ret);
        }


    }


    @Test
    public void getMenuTest() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/menu").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        JSONParser parser = new JSONParser();
        JSONArray menu = (JSONArray) parser.parse(json);
        for (int j = 0; j < menu.size(); j++) {
            MenuItem mi = objectMapper.readValue(menu.get(j).toString(), MenuItem.class);
            int i = foodnames.indexOf(mi.getFoodName());
            for (String key : stands.keySet()) {
                if (stands.get(key).contains(i)) {
                    assertEquals(mi.getFoodName(), foodnames.get(i));
                    assertEquals(mi.getPreptime(), (int) preptimes.get(i));
                    assertEquals(mi.getPrice().round(new MathContext(2)), prices.get(i).round(new MathContext(2)));
                    if (descriptions.get(i).equals("")) assertNull(mi.getDescription());
                    else assertEquals(descriptions.get(i), mi.getDescription());
                    if (mi.getCategory() == null || mi.getCategory().size() == 0)
                        assertEquals("", categories.get(i));
                    else {
                        boolean catOk = false;

                        for (String cat : mi.getCategory()) {
                            if (cat.equals(categories.get(i))) catOk = true;
                        }
                        assertTrue(catOk);
                    }
                }
            }
        }
    }

    @Test
    public void getStandmenusTest() throws Exception {
        for (String key : stands.keySet()) { //check for every standmenu
            MvcResult result = this.mockMvc.perform(get(String.format("/standmenu?standname=%s", key.concat(time))).contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token))
                    .andReturn();
            String json = result.getResponse().getContentAsString();
            MenuItem[] mis = objectMapper.readValue(json, MenuItem[].class);
            for (int j = 0; j < mis.length; j++) { //check through entire menu if every item is present and has right attributes
                MenuItem mi = mis[j];
                int i = foodnames.indexOf(mi.getFoodName());
                assertNotEquals(i, -1);
                assertEquals(mi.getFoodName(), foodnames.get(i));
                assertEquals(mi.getPreptime(), (int) preptimes.get(i));
                assertEquals(mi.getPrice().round(new MathContext(2)), prices.get(i).round(new MathContext(2)));
                if (descriptions.get(i).equals("")) assertNull(mi.getDescription());
                else assertEquals(descriptions.get(i), mi.getDescription());
                if (mi.getCategory() == null || mi.getCategory().size() == 0) assertEquals("", categories.get(i));
                else {
                    boolean catOk = false;

                    for (String cat : mi.getCategory()) {
                        if (cat.equals(categories.get(i))) catOk = true;
                    }
                    assertTrue(catOk);
                }

            }
        }
    }

    @Test
    public void alterMenus() throws Exception {
        Map<String, StandInfo> standInfos = new HashMap<>();
        ArrayList<String> foodnames2 = new ArrayList<String>(Arrays.asList("burger1", "burger2", "pizza3", "pizza2", "cola", "fries"));

        ArrayList<BigDecimal> prices2 = new ArrayList<BigDecimal>(Arrays.asList(BigDecimal.valueOf(100.0), BigDecimal.valueOf(-1.0), BigDecimal.valueOf(12.0), BigDecimal.valueOf(13.0), BigDecimal.valueOf(2.5), BigDecimal.valueOf(4.0)));
        ArrayList<Integer> preptimes2 = new ArrayList<Integer>(Arrays.asList(-1, 11, 12, 13, 2, 40));
        ArrayList<String> categories2 = new ArrayList<String>(Arrays.asList("bacon", "", "pizza", "pizza", "drink", ""));
        ArrayList<String> descriptions2 = new ArrayList<String>(Arrays.asList("burger with bacon", "", "pizza with salami", "pizza with extra pineapple", "", "with ketchup"));

        int n = 2;
        for (String key : stands.keySet()) {
            StandInfo si = new StandInfo(key.concat(time), key.split("-")[0].concat(time), (long) n * 10, (long) -n * 10);
            standInfos.put(key, si);
            n++;
        }
        for (int i = 0; i < foodnames2.size(); i++) {
            for (String key : stands.keySet()) {
                if (stands.get(key).contains(i)) {
                    List<String> cat = new ArrayList<>();
                    cat.add(categories2.get(i));
                    MenuItem mi = new MenuItem(foodnames2.get(i), prices2.get(i), preptimes2.get(i), 20, key.split("-")[0], descriptions2.get(i), cat);
                    standInfos.get(key).addMenuItem(mi);
                }
            }

        }

        for (String key : stands.keySet()) {
            MvcResult result = this.mockMvc.perform(post("/addstand").contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token).content(objectMapper.writeValueAsString(standInfos.get(key)))).andReturn();
            String ret = result.getResponse().getContentAsString();
            assertTrue(ret.equals("Saved"));
        }

        MvcResult result = this.mockMvc.perform(get("/menu").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        JSONParser parser = new JSONParser();
        JSONArray menu = (JSONArray) parser.parse(json);
        for (int j = 0; j < menu.size(); j++) {
            MenuItem mi = objectMapper.readValue(menu.get(j).toString(), MenuItem.class);
            int i = foodnames2.indexOf(mi.getFoodName());
            int k = foodnames.indexOf(mi.getFoodName());
            if (i == -1)
                assertEquals(i, k); //if a food item in menu isnt part of new test lists, it shouldnt be part of old test lists (if it was in old lists and not in new list, it should be removed from menu)
            for (String key : stands.keySet()) {
                if (stands.get(key).contains(i)) {
                    assertEquals(mi.getFoodName(), foodnames2.get(i));
                    if (preptimes2.get(i) < 0) assertEquals(mi.getPreptime(), (int) preptimes.get(i));
                    else assertEquals(mi.getPreptime(), (int) preptimes2.get(i));
                    if (prices2.get(i).compareTo(BigDecimal.ZERO) > 0)
                        assertEquals(mi.getPrice().round(new MathContext(2)), prices2.get(i).round(new MathContext(2)));
                    else
                        assertEquals(mi.getPrice().round(new MathContext(2)), prices.get(i).round(new MathContext(2)));
                    if (descriptions2.get(i).equals("")) {
                        if (descriptions.get(i).equals("")) assertNull(mi.getDescription());
                        else assertEquals(descriptions.get(i), mi.getDescription());

                    } else assertEquals(descriptions2.get(i), mi.getDescription());

                    if (mi.getCategory() == null || mi.getCategory().size() == 0) {
                        assertEquals("", categories.get(i));
                        assertEquals("", categories2.get(i));
                    } else {
                        boolean catOk = false;
                        boolean cat2Ok = false;
                        for (String cat : mi.getCategory()) {
                            if (cat.equals(categories.get(i))) catOk = true;
                            if (cat.equals(categories2.get(i))) cat2Ok = true;
                        }
                        if (!categories.get(i).equals("")) assertTrue(catOk);
                        if (!categories2.get(i).equals("")) assertTrue(cat2Ok);

                    }

                }
            }
        }


    }

    /**
     * delete the tested stands
     *
     * @throws Exception
     */
    @After
    public void clearMenus() throws Exception {
        for (String key : stands.keySet()) {
            MvcResult result = this.mockMvc.perform(get(String.format("/deleteStand?standname=%s", key.concat(time))).contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token)).andReturn();
            String ret1 = result.getResponse().getContentAsString();
            assertEquals(ret1, "Stand " + key.concat(time) + " deleted.");
        }
    }


}
