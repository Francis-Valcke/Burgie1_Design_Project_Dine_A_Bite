package cobol.services.ordermanager.controller;

import cobol.commons.CommonStand;
import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.OrderProcessor;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.StandRepository;
import cobol.services.ordermanager.exception.DoesNotExistException;
import cobol.services.ordermanager.exception.DuplicateStandException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class StandController {

    @Autowired
    private MenuHandler menuHandler;

    @Autowired
    private StandRepository standRepository;

    /**
     * This API will add a stand to:
     * - The database
     * - The cache
     *
     * @param stand The stand that needs to be created
     * @return Success message or exception
     * @throws JsonProcessingException Json processing error
     * @throws ParseException          Json parsing error
     * @throws DuplicateStandException Such a stand already exists
     */
    @PostMapping(path = "/addStand")
    @ResponseBody
    public ResponseEntity<String> addStand(@RequestBody CommonStand stand) throws JsonProcessingException, ParseException, DuplicateStandException {
        menuHandler.addStand(stand);
        return ResponseEntity.ok("The stand was created.");
    }


    /**
     * This API will update a stands
     * - The database
     * - The cache
     *
     * @param stand The stand that needs to be created
     * @return Success message or exception
     */
    @PostMapping(path = "/updateStand")
    @ResponseBody
    public ResponseEntity<String> updateStand(@RequestBody CommonStand stand) throws DoesNotExistException, JsonProcessingException {
        menuHandler.updateStand(stand);
        return ResponseEntity.ok("The stand was updated.");
    }


    /**
     * This API will delete a stand based on its Id.
     * The stand will be removed in:
     * - The database
     * - The local cache
     * - The cache of the StandManager
     *
     * @param standName standName
     * @param brandName brandName
     * @return Success message or exception
     * @throws JsonProcessingException Json processing error
     */
    @GetMapping(value = "/deleteStand")
    @ResponseBody
    public ResponseEntity<String> deleteStand(@RequestParam() String standName, @RequestParam String brandName) throws JsonProcessingException, DoesNotExistException {
        menuHandler.deleteStandById(standName, brandName);
        return ResponseEntity.ok("The stand was removed.");
    }


    /**
     * This method will retrieve all stand names with corresponding brand names
     *
     * @return HashMap of "standName":"brandName"
     */
    @GetMapping(value = "/stands")
    public ResponseEntity<Map<String, String>> requestStandNames() {
        return ResponseEntity.ok(standRepository.findAll()
                .stream().collect(Collectors.toMap(Stand::getName, stand -> stand.getBrand().getName())));
    }
}


