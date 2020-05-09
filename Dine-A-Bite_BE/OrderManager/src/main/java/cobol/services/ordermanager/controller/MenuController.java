package cobol.services.ordermanager.controller;

import cobol.commons.CommonFood;
import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.commons.exception.DoesNotExistException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static cobol.commons.stub.OrderManagerStub.GET_MENU;
import static cobol.commons.stub.OrderManagerStub.GET_STAND_MENU;

@RestController
public class MenuController {

    @Autowired
    MenuHandler menuHandler;

    /**
     * This API will retrieve the all of the food items in the system.
     * It will filter items with identical name and brandName's
     *
     * @return Global menu
     */
    @GetMapping(GET_MENU)
    @ResponseBody
    public ResponseEntity<List<CommonFood>> requestGlobalMenu() throws JsonProcessingException {
        return ResponseEntity.ok(menuHandler.getGlobalMenu());
    }

    /**
     * Rest call for retrieving food items of a given stand by it's id.
     *
     * @param standName name of stand
     * @param brandName name of brand
     * @return List of food items
     */
    @GetMapping(GET_STAND_MENU)
    @ResponseBody
    public ResponseEntity<List<CommonFood>> requestStandMenu(@RequestParam String standName, @RequestParam String brandName) throws DoesNotExistException {
        return ResponseEntity.ok(
                menuHandler.getStandMenu(standName, brandName)
                        .stream()
                        .map(Food::asCommonFood)
                        .collect(Collectors.toList())
        );
    }

}
