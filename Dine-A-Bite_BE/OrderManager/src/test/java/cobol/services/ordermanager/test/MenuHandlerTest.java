package cobol.services.ordermanager.test;

import cobol.commons.CommonFood;
import cobol.commons.CommonStand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    //add lists:
    private ArrayList<String> foodnames = new ArrayList<String>(Arrays.asList("burger1", "burger2", "pizza1", "pizza2", "cola", "fries"));
    //the integer list is a list with the indexes of foodnames contained by the stands
    private Map<String, List<Integer>> stands = new HashMap<String, List<Integer>>() {{
        put("burgerstand-1", Arrays.asList(0, 4, 5));
        put("burgerstand-2", Arrays.asList(1, 4, 5));
        put("pizzastand-1", Arrays.asList(2, 3, 4));
    }};
    private ArrayList<BigDecimal> prices = new ArrayList<BigDecimal>(Arrays.asList(BigDecimal.valueOf(10.0), BigDecimal.valueOf(11.0), BigDecimal.valueOf(12.0), BigDecimal.valueOf(13.0), BigDecimal.valueOf(2.5), BigDecimal.valueOf(4.0)));
    private ArrayList<Integer> preptimes = new ArrayList<Integer>(Arrays.asList(10, 11, 12, 13, 2, 4));
    private ArrayList<String> categories = new ArrayList<String>(Arrays.asList("burger", "", "pizza", "pizza", "drink", "fries"));
    private ArrayList<String> descriptions = new ArrayList<String>(Arrays.asList("burger with bacon", "burger with cheese", "pizza with salami", "pizza with pineapple", "", "with frietsauce"));
    //edit lists:
    private ArrayList<String> foodnames2 = new ArrayList<String>(Arrays.asList("burger1", "burger2", "pizza3", "pizza2", "cola", "fries"));
    private ArrayList<BigDecimal> prices2 = new ArrayList<BigDecimal>(Arrays.asList(BigDecimal.valueOf(100.0), BigDecimal.valueOf(-1.0), BigDecimal.valueOf(12.0), BigDecimal.valueOf(13.0), BigDecimal.valueOf(2.5), BigDecimal.valueOf(4.0)));
    private ArrayList<Integer> preptimes2 = new ArrayList<Integer>(Arrays.asList(-1, 11, 12, 13, 2, 40));
    private ArrayList<String> categories2 = new ArrayList<String>(Arrays.asList("bacon", "", "pizza", "pizza", "drink", ""));
    private ArrayList<String> descriptions2 = new ArrayList<String>(Arrays.asList("burger with bacon", "", "pizza with salami", "pizza with extra pineapple", "", "with ketchup"));


    private String time;
    /**
     * setup stands for testing
     *
     * @throws Exception when call failed or assertion wrong
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
        //create a unique attachment so every test is unique in time: with this, 2 of the same tests cannot intefere if executed close (but not exact at same time) in time
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        time=timestamp.toString();
        Map<String, CommonStand> standInfos = new HashMap<>();
        //initialise stands with some variation in coordinates
        int n = 0;
        for (String key : stands.keySet()) {
            CommonStand si = new CommonStand(key.concat(time), key.split("-")[0].concat(time), (long) n * 10, (long) -n * 10);
            standInfos.put(key, si);
            n++;
        }
        //add menuitems to stands with attributes from lists above
        for (int i = 0; i < foodnames.size(); i++) {
            for (String key : stands.keySet()) {
                if (stands.get(key).contains(i)) {
                    List<String> cat = new ArrayList<>();
                    cat.add(categories.get(i));
                    CommonFood mi = new CommonFood(foodnames.get(i), prices.get(i), preptimes.get(i), 20, key.split("-")[0].concat(time), descriptions.get(i), cat);
                    standInfos.get(key).addMenuItem(mi);
                }
            }

        }
        //call addstand method from menuhandler to add stands and check if they are correctly added
        for (String key : stands.keySet()) {
            MvcResult result = this.mockMvc.perform(post("/addstand").contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token).content(objectMapper.writeValueAsString(standInfos.get(key)))).andReturn();
            String ret = result.getResponse().getContentAsString();
            assertEquals("Saved", ret);
        }


    }

    /**
     * request menu to see if items are correct
     * @throws Exception when call failed or assertion wrong
     */
    @Test
    public void getMenuTest() throws Exception {
        //call menu from menuhandler and extract MenuItems
        MvcResult result = this.mockMvc.perform(get("/menu").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        CommonFood[] mis = objectMapper.readValue(json, CommonFood[].class);
        //check through entire menu if every item is present and has right attributes
        for (CommonFood mi : mis) {
            Map<String, Integer> count = new HashMap<>();
            int i = foodnames.indexOf(mi.getName());
            for (String key : stands.keySet()) {
                //check if item part of stand (check if brand and standname correct)
                if (stands.get(key).contains(i) && (key.split("-")[0].concat(time).equals(mi.getBrandName()))) {
                    count.merge(key, 1, Integer::sum);
                    //check if foodname correct
                    assertEquals(mi.getName(), foodnames.get(i));
                    //check if preptime correct
                    assertEquals(mi.getPreparationTime(), (int) preptimes.get(i));
                    //check if price correct
                    assertEquals(mi.getPrice().round(new MathContext(2)), prices.get(i).round(new MathContext(2)));
                    //check if description correct (empty description is not added)
                    if (descriptions.get(i).equals("")) assertNull(mi.getDescription());
                    else assertEquals(descriptions.get(i), mi.getDescription());
                    //check if category correct (empty category is not added)
                    if (mi.getCategory() == null || mi.getCategory().size() == 0)
                        assertEquals("", categories.get(i));
                    else {
                        boolean catOk = false;
                        //looks if added category is part of list
                        for (String cat : mi.getCategory()) {
                            if (cat.equals(categories.get(i))) catOk = true;
                        }
                        assertTrue(catOk);
                    }
                }
            }
            for (String key : count.keySet()) {

                //assertEquals(stands.get(key).size(), (int) count.get(key));
            }
        }
    }

    /**
     * request standmenu to see if items are correct an no extra items are added
     * @throws Exception when call failed or assertion wrong
     */
    @Test
    public void getStandmenusTest() throws Exception {
        for (String key : stands.keySet()) { //check for every standmenu
            // for specific standname, call standmenu and extract result as MenuItems list
            MvcResult result = this.mockMvc.perform(get(String.format("/standmenu?standname=%s", key.concat(time))).contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token))
                    .andReturn();
            String json = result.getResponse().getContentAsString();
            CommonFood[] mis = objectMapper.readValue(json, CommonFood[].class);
            //check through entire menu if every item is present and has right attributes
            for (CommonFood mi : mis) {
                int i = foodnames.indexOf(mi.getName()); //returns -1 if not found
                //check if no extra item added
                assertNotEquals(i, -1);
                //check if brandname correct
                assertEquals(key.split("-")[0].concat(time), mi.getBrandName());
                //check if foodname correct
                assertEquals(mi.getName(), foodnames.get(i));
                //check if preptime correct
                assertEquals(mi.getPreparationTime(), (int) preptimes.get(i));
                //check if price correct
                assertEquals(mi.getPrice().round(new MathContext(2)), prices.get(i).round(new MathContext(2)));
                //check if description correct (empty description is not added -> then check if null)
                if (descriptions.get(i).equals("")) assertNull(mi.getDescription());
                else assertEquals(descriptions.get(i), mi.getDescription());
                //check if category correct (empty category is not added -> then check if null)
                if (mi.getCategory() == null || mi.getCategory().size() == 0) assertEquals("", categories.get(i));
                else {
                    boolean catOk = false;
                    //looks if added category is part of list
                    for (String cat : mi.getCategory()) {
                        if (cat.equals(categories.get(i))) {
                            catOk = true;
                            break;
                        }
                    }
                    assertTrue(catOk);
                }

            }
        }
    }

    /**
     * Edit stands and request menu to see if they are correct
     * @throws Exception when call failed or assertion wrong
     */
    @Test
    public void alterMenus() throws Exception {
        Map<String, CommonStand> standInfos = new HashMap<>();
        //initialise stands with different variation in coordinates
        int n = 2;
        for (String key : stands.keySet()) {
            CommonStand si = new CommonStand(key.concat(time), key.split("-")[0].concat(time), (long) n * 10, (long) -n * 10);
            standInfos.put(key, si);
            n++;
        }
        //add menuitems to stands with attributes from second lists above
        for (int i = 0; i < foodnames2.size(); i++) {
            for (String key : stands.keySet()) {
                if (stands.get(key).contains(i)) {
                    List<String> cat = new ArrayList<>();
                    cat.add(categories2.get(i));
                    CommonFood mi = new CommonFood(foodnames2.get(i), prices2.get(i), preptimes2.get(i), 20, key.split("-")[0].concat(time), descriptions2.get(i), cat);
                    standInfos.get(key).addMenuItem(mi);
                }
            }

        }
        //call addstand method from menuhandler to edit stands and check if they are correctly edited
        for (String key : stands.keySet()) {
            MvcResult result = this.mockMvc.perform(post("/addstand").contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token).content(objectMapper.writeValueAsString(standInfos.get(key)))).andReturn();
            String ret = result.getResponse().getContentAsString();
            assertTrue(ret.equals("Saved"));
        }
        //call menu from menuhandler and extract MenuItems
        MvcResult result = this.mockMvc.perform(get("/menu").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        CommonFood[] mis = objectMapper.readValue(json, CommonFood[].class);
        //check through entire menu if every item is present and has right attributes
        for (CommonFood mi : mis) {
            int i = foodnames2.indexOf(mi.getName());
            int k = foodnames.indexOf(mi.getName());
            if (i == -1) {
                //if a food item in menu isnt part of new test lists, it shouldnt be part of old test lists (if it was in old lists and not in new list, it should be removed from menu)
                assertEquals(i, k);
            }
            Map<String, Integer> count = new HashMap<>();
            for (String key : stands.keySet()) {
                //check if item part of stand (check if brand and standname correct)
                if (stands.get(key).contains(i) && (key.split("-")[0].concat(time).equals(mi.getBrandName()))) {
                    //check if foodname correct, foodnames should only be from second lists now
                    assertEquals(mi.getName(), foodnames2.get(i));
                    //check if preptime correctly edited (only edited if positive)
                    if (preptimes2.get(i) < 0) assertEquals(mi.getPreparationTime(), (int) preptimes.get(i));
                    else assertEquals(mi.getPreparationTime(), (int) preptimes2.get(i));
                    //check if price correctly edited (only edited if positive)
                    if (prices2.get(i).compareTo(BigDecimal.ZERO) > 0)
                        assertEquals(mi.getPrice().round(new MathContext(2)), prices2.get(i).round(new MathContext(2)));
                    else
                        assertEquals(mi.getPrice().round(new MathContext(2)), prices.get(i).round(new MathContext(2)));
                    //check if description correctly edited, only edited if new description not empty. If previously already null and new description is empty then check if it remained null
                    if (descriptions2.get(i).equals("")) {
                        if (descriptions.get(i).equals("")) assertNull(mi.getDescription());
                        else assertEquals(descriptions.get(i), mi.getDescription());

                    } else assertEquals(descriptions2.get(i), mi.getDescription());
                    //check if category correctly added, only added if not empty or null. If the category was previously null and the new categoy is empty it should still be null
                    if (mi.getCategory() == null || mi.getCategory().size() == 0) {
                        assertEquals("", categories.get(i));
                        assertEquals("", categories2.get(i));
                    } else {
                        boolean catOk = false;
                        boolean cat2Ok = false;
                        //if both categories are not empty, then both should be contained in the category list of menuItem
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
     * @throws Exception when call failed or assertion wrong
     */
    @After
    public void clearMenus() throws Exception {
        for (String key : stands.keySet()) {
            //call deleteStand in menuhandler to delete tested stands from database
            MvcResult result = this.mockMvc.perform(get(String.format("/deleteStand?standname=%s", key.concat(time))).contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token)).andReturn();
            String ret1 = result.getResponse().getContentAsString();
            assertEquals(ret1, "Stand " + key.concat(time) + " deleted.");
        }
    }


}
