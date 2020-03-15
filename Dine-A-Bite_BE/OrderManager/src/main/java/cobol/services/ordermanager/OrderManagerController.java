package cobol.services.ordermanager;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class OrderManagerController {
    @RequestMapping("/pingOM")
    public String index() {
        return "Response from Order Manager";
    }

    /**
     * @return global menu in JSON format
     * sent request to localhost:8080/menu
     * This iterates all menus of the schedulers, checks for unique items,
     * and puts these in a JSON file with their price.
     * In the JSON file the keys are the menu item names and the values are the prices
     */
    @RequestMapping("/menu")
    public JSONObject requestTotalMenu() { //start with id=1 (temporary)
        System.out.println("request total menu");
        return MenuHandler.getMenuHandler().getTotalmenu();
    }
    /**
     * @return specific stand menu in JSON format
     * sent POST request to localhost:8080/standmenu
     * @RequestParam() String standname: post the name of a stand
     * (In Postman: Select POST, go to params, enter "standname" as KEY and enter the name of a stand as value)
     * (ex:localhost:8080/standmenu?standname=food1)
     * (in current test above: "food1" and "food2" are names of stands)
     * This iterates menu of the named stand,
     * and puts the menu items in a JSON file with their price.
     * In the JSON file the keys are the menu item names and the values are the prices
     */
    @RequestMapping(value ="/standmenu", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject requestStandMenu(@RequestParam() String standname) {
        System.out.println("request menu of stand " + standname);
        return MenuHandler.getMenuHandler().getStandMenu(standname);
    }

    /**
     *
     * @return names of all stands:
     * key = number in list
     * value = standname
     */
    @RequestMapping("/stands")
    public JSONObject requestStandnames() { //start with id=1 (temporary)
        System.out.println("request stand names");
        return MenuHandler.getMenuHandler().getStandnames();
    }
}
