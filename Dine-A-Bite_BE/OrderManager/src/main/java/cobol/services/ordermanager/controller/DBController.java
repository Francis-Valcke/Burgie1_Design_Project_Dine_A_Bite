package cobol.services.ordermanager.controller;

import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.domain.entity.Brand;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
public class DBController {

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    StandRepository standRepository;

    @Autowired
    MenuHandler menuHandler;

    /**
     * This API will accept a JSON that describes the database contents.
     * This is an easy way to fill the database with contents.
     *
     * @param data List of Brand objects deserialized from json
     * @return Success message or exception
     */
    @PostMapping("/db/import")
    public ResponseEntity<String> load(@RequestBody List<Brand> data) {

        data.forEach(brand -> brandRepository.save(brand));
        return ResponseEntity.ok("Success");
    }

    /**
     * This API will clear all of the database contents.
     * This will not clear the local cache.
     *
     * @return Success message or exception
     */
    @DeleteMapping("/db/clear")
    public ResponseEntity<String> clear() throws ParseException, JsonProcessingException {

        brandRepository.deleteAll();
        return ResponseEntity.ok("Success");

    }

    /**
     * This API will export the database contents in json format.
     *
     * @return Success message or exception
     */
    @GetMapping("/db/export")
    public ResponseEntity<List<Brand>> export() {

        List<Brand> data = brandRepository.findAll();

        return ResponseEntity.ok(data);

    }


    /**
     * This API will refresh:
     * - The cache in StandManager
     * with respect to the database
     *
     * @return List of stand names
     * @throws JsonProcessingException Json processing error
     */
    @RequestMapping(value = "/updateSM", method = RequestMethod.GET)
    public ResponseEntity<List<String>> update() throws JsonProcessingException {
        menuHandler.updateStandManager();

        List<Stand> stands = standRepository.findAll();
        List<String> standNames = stands.stream().map(Stand::getName).collect(Collectors.toList());
        return ResponseEntity.ok(standNames);
    }

    /**
     * This API will clear database contents and the local and StandManager cache.
     *
     * @return Success message or exception
     * @throws ParseException Parsing error
     * @throws JsonProcessingException Json processing error
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete() throws ParseException, JsonProcessingException {
        menuHandler.deleteAll();
        return ResponseEntity.ok("Database from OrderManager and StandManager cleared.");
    }


}
