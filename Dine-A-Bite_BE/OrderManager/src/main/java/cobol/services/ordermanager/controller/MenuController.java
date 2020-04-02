package cobol.services.ordermanager.controller;

import cobol.commons.CommonFood;
import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.exception.DoesNotExistException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    @RequestMapping(value="/menu", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<CommonFood>> requestGlobalMenu() throws JsonProcessingException { //start with id=1 (temporary)
        return ResponseEntity.ok(menuHandler.getGlobalMenu());
    }

    /**
     * Rest call for retrieving food items of a given stand by it's id.
     *
     * @param standName name of stand
     * @param brandName name of brand
     * @return List of food items
     */
    @GetMapping(value = "/standMenu")
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
