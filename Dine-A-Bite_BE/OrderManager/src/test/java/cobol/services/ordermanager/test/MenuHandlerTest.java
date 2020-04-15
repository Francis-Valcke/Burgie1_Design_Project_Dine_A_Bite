package cobol.services.ordermanager.test;

import cobol.commons.CommonFood;
import cobol.commons.CommonStand;
import cobol.services.ordermanager.domain.entity.Brand;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.CategoryRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Assert;
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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


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
    StandRepository standRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private MockMvc mockMvc;
    private String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlVTRVIiLCJTVEFORCIsIkFETUlOIl0sImlhdCI6MTU4Njg1NDgyNSwiZXhwIjoxNzQ0NTM0ODI1fQ.TJrhAEF95JQ9k10HWdn9FdLTcgmq909WDWr51AQAPPE";

    private static List<Brand> brands = new ArrayList<>();

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
        brands = mapper.readValue(body, new TypeReference<List<Brand>>() {
        });


        this.mockMvc.perform(post("/db/import").contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

    }


    /**
     * request menu to see if items are correct
     *
     * @throws Exception when call failed or assertion wrong
     */
    @Test
    public void getMenuTest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        //call menu from menuhandler and extract MenuItems
        MvcResult result = this.mockMvc.perform(get("/menu").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token))
                .andExpect(status().isOk())
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

    }

    /**
     * request standmenu to see if items are correct an no extra items are added
     *
     * @throws Exception when call failed or assertion wrong
     */
    @Test
    public void getStandmenusTest() throws Exception {

        for (Brand brand : brands) {
            for (Stand stand : brand.getStandList()) {
                MvcResult result = this.mockMvc.perform(get("/standMenu").contentType(MediaType.APPLICATION_JSON)
                        .queryParam("standName", stand.getName())
                        .queryParam("brandName", stand.getBrandName())
                        .header("Authorization", token))
                        .andExpect(status().isOk())
                        .andReturn();

                String standMenuJson = result.getResponse().getContentAsString();
                List<CommonFood> standMenu = objectMapper.readValue(standMenuJson, new TypeReference<List<CommonFood>>() {
                });


                CommonStand cStand = stand.asCommonStand();

                assert (cStand.getMenu().containsAll(standMenu) && standMenu.containsAll(cStand.getMenu()));

            }
        }


    }


    @Test
    public void alterMenus() throws Exception {

        // ---- UPDATE MENU ----//

        URL url = Thread.currentThread().getContextClassLoader().getResource("newmenu.json");
        assert url != null;
        String body = Resources.toString(url, StandardCharsets.UTF_8);
        CommonStand incomingStand = objectMapper.readValue(body, CommonStand.class);

        this.mockMvc.perform(post("/updateStand").contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();


        // ---- RETRIEVE STAND AND CHECK IF UPDATES PERSISTED ---- //

        MvcResult result = this.mockMvc.perform(get("/standMenu").contentType(MediaType.APPLICATION_JSON)
                .queryParam("standName", incomingStand.getName())
                .queryParam("brandName", incomingStand.getBrandName())
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        String updatedIncomingStand = result.getResponse().getContentAsString();
        List<CommonFood> updatedIncomingStandMenu = objectMapper.readValue(updatedIncomingStand, new TypeReference<List<CommonFood>>() {
        });

        System.out.println();
        assert (incomingStand.getMenu().containsAll(updatedIncomingStandMenu) && updatedIncomingStandMenu.containsAll(incomingStand.getMenu()));


        // ---- CHECK IF ALL STANDS WITH THE SAME FOOD ITEM WHERE UPDATED AS WELL ---- //
        Brand brand = brands.stream().filter(b -> b.getName().equals(incomingStand.getBrandName())).findFirst().get();

        for (Stand oldStand : brand.getStandList()) {
            MvcResult standResult = this.mockMvc.perform(get("/standMenu").contentType(MediaType.APPLICATION_JSON)
                    .queryParam("standName", oldStand.getName())
                    .queryParam("brandName", oldStand.getBrandName())
                    .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andReturn();

            String updatedStandFromBrand = result.getResponse().getContentAsString();
            List<CommonFood> updatedStandFoodFromBrand = objectMapper.readValue(updatedStandFromBrand, new TypeReference<List<CommonFood>>() {
            });

            for (CommonFood possibleUpdatedFood : updatedStandFoodFromBrand) {
                // search in requested change list and compare with retrieved menu
                for (CommonFood updatedFoodIncoming : incomingStand.getMenu()) {
                    if(updatedFoodIncoming.getName().equals(possibleUpdatedFood.getName())){
                        assert (possibleUpdatedFood.equals(updatedFoodIncoming));


                        // if we see the oldStand which was also the incoming order, check if the stock was updated
                        if(oldStand.getName().equals(incomingStand.getName())){
                            // search for old food item
                            Food oldFood= oldStand.getFoodList().stream().filter(f -> f.getName().equals(updatedFoodIncoming.getName())).findAny().orElse(null);
                            if(oldFood != null) {
                                int sum= oldFood.getStock()+ updatedFoodIncoming.getStock();
                                int newStock= possibleUpdatedFood.getStock();

                                assert sum==newStock;
                            }

                        }
                    }


                }
            }
        }

    }


    @Test
    public void addStand() throws Exception{
        URL url = Thread.currentThread().getContextClassLoader().getResource("newStand.json");
        assert url != null;
        String body = Resources.toString(url, StandardCharsets.UTF_8);
        CommonStand commonStand = objectMapper.readValue(body, CommonStand.class);

        this.mockMvc.perform(post("/addStand").contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
    }


    @Test
    public void addTwoStandsFromSameBrand() throws Exception {
        // TODO: uncomment when merging with brand of francis
        URL url = Thread.currentThread().getContextClassLoader().getResource("newStand.json");
        assert url != null;
        String body = Resources.toString(url, StandardCharsets.UTF_8);
        CommonStand commonStand = objectMapper.readValue(body, CommonStand.class);

        this.mockMvc.perform(post("/addStand").contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        URL url2 = Thread.currentThread().getContextClassLoader().getResource("newStand2.json");
        assert url2 != null;
        String body2 = Resources.toString(url2, StandardCharsets.UTF_8);
        CommonStand commonStand2 = objectMapper.readValue(body, CommonStand.class);

        this.mockMvc.perform(post("/addStand").contentType(MediaType.APPLICATION_JSON)
                .content(body2)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

    }


    @Test
    public void deleteStandById() throws Exception {
        this.mockMvc.perform(delete("/deleteStand")
                .queryParam("standName", "BurgerKing 1")
                .queryParam("brandName", "BurgerKing")
                .header("Authorization", token))
                .andExpect(status().isOk()).andReturn();

        Stand stand= standRepository.findStandById("BurgerKing 1","BurgerKing").orElse(null);
        Assert.assertNull(stand);

    }



    @After
    public void clean() {
        brandRepository.deleteAll();
    }
}





