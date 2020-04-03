package cobol.services.ordermanager.test;

import cobol.commons.CommonFood;
import cobol.commons.CommonStand;
import cobol.services.ordermanager.domain.entity.Brand;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.CategoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
 * Testset should cover all cases that can be transmitted from apps
 * Tests in this class:
 * Stands correctly added - check
 * Menu items correctly added:
 * 1. in global menu: items with same name from different brands appear once for every brand - check
 * 2. in stand menu: all items appear from requested stand and none from other stands -check
 * 3. All items that should be in menu are in the menu - check
 * 4. No duplicates in menu (items unique for each brand) - check
 * Menu items correcty changed:
 * 1. if new value is -1 for price or preptime, or "" for categories or description, then the old value is not changed -check
 * 2. if category has changed, it is added to list- check
 * 3. new items added -check
 * 4. items deleted if not present in new menu -check
 * Stands correctly deleted - check
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MenuHandlerTest {
    @Autowired
    WebApplicationContext applicationContext;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private MockMvc mockMvc;
    private String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmcmFuY2lzIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYXQiOjE1ODQ2MTAwMTcsImV4cCI6MTc0MjI5MDAxN30.5UNYM5Qtc4anyHrJXIuK0OUlsbAPNyS9_vr-1QcOWnQ";

    private static List<Stand> stands = new ArrayList<>();

    /**
     * setup stands for testing
     *
     * @throws Exception when call failed or assertion wrong
     */
    @Before
    public void setup() throws Exception {
        //setup mockmvc of OrderManager application
        mockMvc = webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        URL url = Thread.currentThread().getContextClassLoader().getResource("dataset.json");
        String body = Resources.toString(url, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        List<Brand> brands = mapper.readValue(body, new TypeReference<List<Brand>>() {
        });
        for (Brand brand : brands) {
            stands.addAll(brand.getStandList());
        }


         this.mockMvc.perform(post("/db/import").contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", token))
                .andReturn();

    }


    /**
     * request menu to see if items are correct
     *
     * @throws Exception when call failed or assertion wrong
     */
    @Test
    public void getMenuTest() throws Exception {


        ObjectMapper mapper= new ObjectMapper();
        //call menu from menuhandler and extract MenuItems
        MvcResult result = this.mockMvc.perform(get("/menu").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        List<CommonFood> retrievedFoodItems = mapper.readValue(json, new TypeReference<List<CommonFood>>() {
        });

        // What the result should be
        URL url2 = Thread.currentThread().getContextClassLoader().getResource("globalmenu.json");
        String body2 = Resources.toString(url2, StandardCharsets.UTF_8);
        List<CommonFood> correctFoodItems = mapper.readValue(body2, new TypeReference<List<CommonFood>>() {
        });


        assert (retrievedFoodItems.containsAll(correctFoodItems) && correctFoodItems.containsAll(retrievedFoodItems));
        brandRepository.deleteAll();
        categoryRepository.deleteAll();

    }

    /**
     * request standmenu to see if items are correct an no extra items are added
     *
     * @throws Exception when call failed or assertion wrong
     */
    @Test
    public void getStandmenusTest() throws Exception {

        for (Stand stand : stands) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/standMenu");
            builder.queryParam("standName",  stand.getName())
                    .queryParam("brandName", stand.getBrandName());
            String uri= builder.toUriString();

            StringBuilder request= new StringBuilder();
            request.append("/standMenu").append("?")
                    .append("standName=").append(stand.getName())
                    .append("&")
                    .append("brandName=").append(stand.getBrandName());


            MvcResult result = this.mockMvc.perform(get(request.toString()).contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token))
                    .andReturn();

            String standMenuJson = result.getResponse().getContentAsString();
            List<CommonFood> standMenu = objectMapper.readValue(standMenuJson, new TypeReference<List<CommonFood>>() {
            });


            CommonStand cStand = stand.asCommonStand();

            assert (cStand.getMenu().containsAll(standMenu) && standMenu.containsAll(cStand.getMenu()));

        }

    }


}

/**
 * Edit stands and request menu to see if they are correct
 *
 * @throws Exception when call failed or assertion wrong
 */
    /*@Test
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
                    CommonFood mi = new CommonFood(foodnames2.get(i), prices2.get(i), preptimes2.get(i), 20, key.split("-")[0].concat(time), key.split("-")[1], descriptions2.get(i), cat);
                    standInfos.get(key).addMenuItem(mi);
                }
            }

        }
        //call addstand method from menuhandler to edit stands and check if they are correctly edited
        for (String key : stands.keySet()) {
            MvcResult result = this.mockMvc.perform(post("/updateStand").contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token).content(objectMapper.writeValueAsString(standInfos.get(key)))).andExpect(status().isOk()).andReturn();
        }
        //call menu from menuhandler and extract MenuItems
        MvcResult result = this.mockMvc.perform(get("/menu").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        CommonFood[] mis = objectMapper.readValue(json, CommonFood[].class);
        Map<String, Integer> count= new HashMap<>();
        //check through entire menu if every item is present and has right attributes
        for (CommonFood mi : mis) {
            int i = foodnames2.indexOf(mi.getName());
            int k = foodnames.indexOf(mi.getName());
            if (i == -1) {
                //if a food item in menu isnt part of new test lists, it shouldnt be part of old test lists (if it was in old lists and not in new list, it should be removed from menu)
                assertEquals(i, k);
                continue;
            }
            for (String key : stands.keySet()) {
                //check if item part of stand (check if brand and standname correct)
                if (stands.get(key).contains(i) && (key.split("-")[0].concat(time).equals(mi.getBrandName()))) {
                    //count items in stand
                    count.merge(key, 1, Integer::sum);
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
        //check for every stand if amount of items in stand is correct
        for (String key : count.keySet()) {
            assertEquals(stands.get(key).size(), (int) count.get(key));
        }


    }

*/



