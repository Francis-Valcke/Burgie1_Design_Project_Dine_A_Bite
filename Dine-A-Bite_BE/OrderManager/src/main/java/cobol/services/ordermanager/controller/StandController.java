package cobol.services.ordermanager.controller;

import cobol.commons.CommonStand;
import cobol.commons.ResponseModel;
import cobol.commons.exception.CommunicationException;
import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.entity.User;
import cobol.services.ordermanager.domain.repository.StandRepository;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.exception.DuplicateStandException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static cobol.commons.ResponseModel.status.ERROR;
import static cobol.commons.ResponseModel.status.OK;

@RestController
public class StandController {

    @Autowired
    private MenuHandler menuHandler;

    @Autowired
    private StandRepository standRepository;

    @GetMapping(path = "/verify")
    public ResponseEntity<HashMap<Object,Object>> verify(@RequestParam String standName, @RequestParam String brandName, @AuthenticationPrincipal User user){

        Stand stand = standRepository.findStandById(standName, brandName).orElse(null);

        if (stand == null) {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK.toString())
                            .details("The stand does not exist and is free to be created.")
                            .build().generateResponse()
            );
        } else if (stand.getOwners().contains(user)) {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(OK.toString())
                            .details("The currently authenticated user is a verified owner of this stand.")
                            .build().generateResponse()
            );
        } else {
            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .status(ERROR.toString())
                            .details("This currently authenticated user is not a owner of this stand.")
                            .build().generateResponse()
            );
        }
    }

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
    public ResponseEntity<HashMap<Object,Object>> addStand(@RequestBody CommonStand stand) throws JsonProcessingException, ParseException, DuplicateStandException, CommunicationException {
        menuHandler.addStand(stand);
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("The stand was added.")
                        .build().generateResponse()
        );
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
    public ResponseEntity<HashMap<Object,Object>> updateStand(@RequestBody CommonStand stand) throws DoesNotExistException, JsonProcessingException {
        menuHandler.updateStand(stand);
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("The stand was updated.")
                        .build().generateResponse()
        );
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
    @DeleteMapping(value = "/deleteStand")
    @ResponseBody
    public ResponseEntity<HashMap<Object,Object>> deleteStand(@RequestParam String standName, @RequestParam String brandName) throws JsonProcessingException, DoesNotExistException {
        menuHandler.deleteStandById(standName, brandName);
        return ResponseEntity.ok(
                ResponseModel.builder()
                        .status(OK.toString())
                        .details("The stand was removed.")
                        .build().generateResponse()
        );
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

    @GetMapping(value = "/standLocations")
    public ResponseEntity<Map<String, Map<String, Double>>> requestStandLocations() {
        return ResponseEntity.ok(standRepository.findAll()
                .stream().collect(Collectors.toMap(Stand::getName, Stand::getLocation)));
    }
}


