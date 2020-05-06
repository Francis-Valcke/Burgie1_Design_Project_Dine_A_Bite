package cobol.services.ordermanager.controller;

import cobol.commons.BetterResponseModel;
import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.domain.entity.Brand;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import cobol.services.ordermanager.domain.repository.UserRepository;
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
    UserRepository userRepository;

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
    public ResponseEntity<BetterResponseModel<?>> load(@RequestBody List<Brand> data) {
        //Add stand as default owner
        //User user = userRepository.save(User.builder().username("stand").build());
        //data.stream().flatMap(brand -> brand.getStandList().stream()).forEach(stand -> stand.getOwners().add(user));
        try{
            data.forEach(brand -> brandRepository.save(brand));
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Exception thrown while importing data to database", e));
        }
        return ResponseEntity.ok(BetterResponseModel.ok("Data is successfully imported", null));
    }

    /**
     * This API will clear all of the database contents.
     * This will not clear the local cache.
     *
     * @return Success message or exception
     */
    @DeleteMapping("/db/clear")
    public ResponseEntity<BetterResponseModel<?>> clear() {
        try{
            brandRepository.deleteAll();
        }
        catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Exception thrown while clearing database", e));
        }
        return ResponseEntity.ok(BetterResponseModel.ok("Brands, stands and fooditems are successfully removed from the database", null));
    }

    /**
     * This API will export the database contents in json format.
     *
     * @return Success message or exception
     */
    @GetMapping("/db/export")
    public ResponseEntity<BetterResponseModel<List<Brand>>> export() {
        List<Brand> data = null;
        try {
            data = brandRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
           return ResponseEntity.ok(BetterResponseModel.error("Exception thrown while retrieving all data from database", e));
        }
        return ResponseEntity.ok(BetterResponseModel.ok("Retrieved data from database", data));
    }


    /**
     * This API will refresh:
     * - The cache in StandManager
     * with respect to the database
     *
     * @return List of stand names
     */
    @RequestMapping(value = "/updateSM", method = RequestMethod.GET)
    public ResponseEntity<BetterResponseModel<List<String>>> update() {
        List<String> standNames=null;
        try {
            menuHandler.updateStandManager();
            List<Stand> stands = standRepository.findAll();
            standNames = stands.stream().map(Stand::getName).collect(Collectors.toList());
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error thrown while updating Stand Manager", e));
        }
        return ResponseEntity.ok(BetterResponseModel.ok("Successfully updated stand manager schedulers", standNames));
    }

    /**
     * This API will clear database contents and the local and StandManager cache.
     *
     * @return Success message or exception
     */
    @DeleteMapping("/delete")
    public ResponseEntity<BetterResponseModel<?>> delete() {
        try {
            menuHandler.deleteAll();
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error thrown while updating Stand Manager", e));
        }
        return ResponseEntity.ok(BetterResponseModel.ok("Database from OrderManager and StandManager cleared.", null));
    }
}
