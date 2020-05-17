package cobol.services.ordermanager.controller;

import cobol.commons.BetterResponseModel;
import cobol.commons.CommonFood;
import cobol.commons.exception.DoesNotExistException;
import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.domain.entity.Food;
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
    public ResponseEntity<BetterResponseModel<List<CommonFood>>> requestGlobalMenu() {

        List<CommonFood> globalMenu= null;
        try {
            globalMenu = menuHandler.getGlobalMenu();
        } catch (DoesNotExistException e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error thrown while fetching global menu", e));
        }
        return ResponseEntity.ok(BetterResponseModel.ok("Successfully retrieved global menu", globalMenu));
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
    public ResponseEntity<BetterResponseModel<List<CommonFood>>> requestStandMenu(@RequestParam String standName, @RequestParam String brandName) {

        List<CommonFood> standMenu= null;
        try {
            standMenu = menuHandler.getStandMenu(standName, brandName)
                    .stream()
                    .map(Food::asCommonFood)
                    .collect(Collectors.toList());
        } catch (DoesNotExistException e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error thrown while fetching stand menu", e));
        }

        return ResponseEntity.ok(BetterResponseModel.ok("Successfully retrieved stand menu", standMenu));
    }

}
