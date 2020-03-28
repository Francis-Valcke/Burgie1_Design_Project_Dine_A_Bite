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
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
public class MenuHandlerTest {
    @Autowired
    WebApplicationContext applicationContext;
    @Autowired
    ObjectMapper objectMapper;
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
    private ArrayList<String> categories = new ArrayList<String>(Arrays.asList(new String[]{"burger", "", "pizza", "pizza", "drink", "fries"}));
    private ArrayList<String> descriptions = new ArrayList<String>(Arrays.asList(new String[]{"burger with bacon", "burger with cheese", "pizza with salami", "pizza with pineapple", "", "with frietsauce"}));

    /**
     * setup stands for testing
     *
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(this.applicationContext)
                .apply(springSecurity())
                .build();
        this.mockMvc.perform(get("/SMswitch?on=false").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token));
        Map<String, StandInfo> standInfos = new HashMap<>();
        int n = 0;
        for (String key : stands.keySet()) {
            StandInfo si = new StandInfo(key, key.split("-")[0], (long) 0.0 + n * 10, (long) 0.0 - n * 10);
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
            assertTrue(ret.equals("Saved"));
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
                    assertTrue(mi.getFoodName().equals(foodnames.get(i)));
                    assertTrue(mi.getPreptime() == preptimes.get(i));
                    assertTrue(mi.getPrice().round(new MathContext(2)).equals(prices.get(i).round(new MathContext(2))));
                    if (descriptions.get(i).equals("")) assertTrue(mi.getDescription() == null);
                    else assertTrue(descriptions.get(i).equals(mi.getDescription()));
                    if (mi.getCategory() == null || mi.getCategory().size() == 0)
                        assertTrue(categories.get(i).equals(""));
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
            MvcResult result = this.mockMvc.perform(get(String.format("/standmenu?standname=%s", key)).contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token))
                    .andReturn();
            String json = result.getResponse().getContentAsString();
            JSONParser parser = new JSONParser();
            JSONArray menu = (JSONArray) parser.parse(json);
            for (int j = 0; j < menu.size(); j++) { //check through entire menu if every item is present and has right attributes
                MenuItem mi = objectMapper.readValue(menu.get(j).toString(), MenuItem.class);
                int i = foodnames.indexOf(mi.getFoodName());
                assertFalse(i == -1);
                assertTrue(mi.getFoodName().equals(foodnames.get(i)));
                assertTrue(mi.getPreptime() == preptimes.get(i));
                assertTrue(mi.getPrice().round(new MathContext(2)).equals(prices.get(i).round(new MathContext(2))));
                if (descriptions.get(i).equals("")) assertTrue(mi.getDescription() == null);
                else assertTrue(descriptions.get(i).equals(mi.getDescription()));
                if (mi.getCategory() == null || mi.getCategory().size() == 0) assertTrue(categories.get(i).equals(""));
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
        ArrayList<String> foodnames2 = new ArrayList<String>(Arrays.asList(new String[]{"burger1", "burger2", "pizza3", "pizza2", "cola", "fries"}));

        ArrayList<BigDecimal> prices2 = new ArrayList<BigDecimal>(Arrays.asList(new BigDecimal[]{BigDecimal.valueOf(100.0), BigDecimal.valueOf(-1.0), BigDecimal.valueOf(12.0), BigDecimal.valueOf(13.0), BigDecimal.valueOf(2.5), BigDecimal.valueOf(4.0)}));
        ArrayList<Integer> preptimes2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, 11, 12, 13, 2, 40}));
        ArrayList<String> categories2 = new ArrayList<String>(Arrays.asList(new String[]{"bacon", "", "pizza", "pizza", "drink", ""}));
        ArrayList<String> descriptions2 = new ArrayList<String>(Arrays.asList(new String[]{"burger with bacon", "", "pizza with salami", "pizza with extra pineapple", "", "with ketchup"}));

        int n = 2;
        for (String key : stands.keySet()) {
            StandInfo si = new StandInfo(key, key.split("-")[0], (long) 0.0 + n * 10, (long) 0.0 - n * 10);
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
                assertTrue(i == k); //if a food item in menu isnt part of new test lists, it shouldnt be part of old test lists (if it was in old lists and not in new list, it should be removed from menu)
            for (String key : stands.keySet()) {
                if (stands.get(key).contains(i)) {
                    assertTrue(mi.getFoodName().equals(foodnames2.get(i)));
                    if (preptimes2.get(i) < 0) assertTrue(mi.getPreptime() == preptimes.get(i));
                    else assertTrue(mi.getPreptime() == preptimes2.get(i));
                    if (prices2.get(i).compareTo(BigDecimal.ZERO) > 0)
                        assertTrue(mi.getPrice().round(new MathContext(2)).equals(prices2.get(i).round(new MathContext(2))));
                    else
                        assertTrue(mi.getPrice().round(new MathContext(2)).equals(prices.get(i).round(new MathContext(2))));
                    if (descriptions2.get(i).equals("")) {
                        if (descriptions.get(i).equals("")) assertTrue(mi.getDescription() == null);
                        else assertTrue(descriptions.get(i).equals(mi.getDescription()));

                    } else assertTrue(descriptions2.get(i).equals(mi.getDescription()));

                    if (mi.getCategory() == null || mi.getCategory().size() == 0) {
                        assertTrue(categories.get(i).equals(""));
                        assertTrue(categories2.get(i).equals(""));
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
            MvcResult result = this.mockMvc.perform(get(String.format("/deleteStand?standname=%s", key)).contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token)).andReturn();
            String ret1 = result.getResponse().getContentAsString();
            assertTrue(ret1.equals("Stand " + key + " deleted."));
        }
    }


}
