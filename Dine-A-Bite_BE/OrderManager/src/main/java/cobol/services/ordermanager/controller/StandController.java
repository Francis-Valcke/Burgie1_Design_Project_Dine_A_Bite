package cobol.services.ordermanager.controller;

import cobol.commons.BetterResponseModel;
import cobol.commons.CommonStand;
import cobol.commons.exception.DoesNotExistException;
import cobol.commons.order.CommonOrder;
import cobol.commons.security.CommonUser;
import cobol.services.ordermanager.MenuHandler;
import cobol.services.ordermanager.domain.entity.Order;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.entity.User;
import cobol.services.ordermanager.domain.repository.StandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class StandController {

    @Autowired
    private MenuHandler menuHandler;

    @Autowired
    private StandRepository standRepository;

    /**
     * This API is used to verify if a authenticated user is the owner of a given stand. This check is needed to allow
     * modifications.
     * @param standName standName
     * @param brandName brandName
     * @param authenticatedUser authenticatedUser
     * @return Success or fail
     */
    @GetMapping(path = "/verify")
    public ResponseEntity<BetterResponseModel<?>> verify(@RequestParam String standName, @RequestParam String brandName, @AuthenticationPrincipal CommonUser authenticatedUser) {

        Stand stand = standRepository.findStandById(standName, brandName).orElse(null);
        User user = new User(authenticatedUser); //Convert to real User object to be able to compare

        if (stand == null) {
            return ResponseEntity.ok(BetterResponseModel.ok("The stand does not exist and is free to be created", null));
        } else if (stand.getOwners().contains(user)) {
            return ResponseEntity.ok(BetterResponseModel.ok("The currently authenticated user is a verified owner of this stand.", null));
        } else {
            return ResponseEntity.ok(BetterResponseModel.error("This currently authenticated user is not an owner of this stand", null));
        }
    }

    /**
     * This API will add a stand to:
     * - The database
     * - The cache
     *
     * @param stand The stand that needs to be created
     * @return Success message or exception
     */
    @PostMapping(path = "/addStand")
    @ResponseBody
    public ResponseEntity<BetterResponseModel<?>> addStand(@RequestBody CommonStand stand, @AuthenticationPrincipal CommonUser user) {

        try {
            menuHandler.addStand(stand, user);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while adding a stand", throwable));
        }

        return ResponseEntity.ok(BetterResponseModel.ok("Stand successfully added", null));


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
    public ResponseEntity<BetterResponseModel<?>> updateStand(@RequestBody CommonStand stand) {
        try {
            menuHandler.updateStand(stand);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while updating a stand", throwable));
        }


        return ResponseEntity.ok(BetterResponseModel.ok("The stand was updated", null));
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
     */
    @DeleteMapping(value = "/deleteStand")
    @ResponseBody
    public ResponseEntity<BetterResponseModel<?>> deleteStand(@RequestParam String standName, @RequestParam String brandName) {
        try {
            menuHandler.deleteStandById(standName, brandName);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while deleting stand", throwable));
        }

        return ResponseEntity.ok(BetterResponseModel.ok("Successfully deleted stand", null));

    }


    /**
     * This method will retrieve all stand names with corresponding brand names
     *
     * @return HashMap of "standName":"brandName"
     */
    @GetMapping(value = "/stands")
    public ResponseEntity<BetterResponseModel<Map<String, String>>> requestStandNames() {
        Map<String, String> stands = standRepository.findAll()
                .stream().collect(Collectors.toMap(Stand::getName, stand -> stand.getBrand().getName()));
        if (stands.isEmpty()) {
            System.out.println("ERROR: no stands found");
            return ResponseEntity.ok(BetterResponseModel.error("No stands found", null));
        } else {
            return ResponseEntity.ok(BetterResponseModel.ok("Successfully retrieved the stands", stands));
        }

    }

    /**
     * Returns the locations of all the stands in the database
     *
     * @return HashMap of String: Map <string, double>. eg: {Stand 1: {latitude: 360, longitude: 360}}
     */
    @GetMapping(value = "/standLocations")
    public ResponseEntity<BetterResponseModel<Map<String, Map<String, Double>>>> requestStandLocations() {

        Map<String, Map<String, Double>> locations = standRepository.findAll()
                .stream().collect(Collectors.toMap(Stand::getName, Stand::getLocation));

        if (locations.isEmpty()) {
            System.out.println("ERROR: no locations found");
            return ResponseEntity.ok(BetterResponseModel.error("No locations found", null));
        } else {
            return ResponseEntity.ok(BetterResponseModel.ok("Successfully retrieved locations", locations));
        }


    }

    /**
     * This method returns the revenue that is stored in the database for a certain stand
     *
     * @param standName: Stand for which the revenue is needee
     * @param brandName: brandname of the stand
     * @return BigDecimal: value of the revenue, wrapped in a BetterResponseModel
     */
    @GetMapping(value = "/revenue")
    @ResponseBody
    public ResponseEntity<BetterResponseModel<BigDecimal>> requestRevenue(@RequestParam String standName, @RequestParam String brandName) {
        Optional<Stand> stand = standRepository.findStandById(standName, brandName);
        BigDecimal revenue;
        if (stand.isPresent() && stand.get().getRevenue() != null) {
            revenue = stand.get().getRevenue();
            return ResponseEntity.ok(BetterResponseModel.ok("Successfully retrieved revenue from database",revenue));
        } else {
            DoesNotExistException e=new DoesNotExistException("Such stand does not exist");
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while retrieving revenue from database", e));
        }
    }

    /**
     * This method is used to retrieve orders from a specific stand that were persisted in the database.
     *
     * @param standName: Stand for which the orders are needed
     * @param brandName: brandname of the stand
     * @return List of CommonOrders
     */
    @GetMapping(value = "/getStandOrders", produces = "application/json")
    public ResponseEntity<BetterResponseModel<List<CommonOrder>>> getUserOrders(@RequestParam(name = "standName") String standName,
                                                           @RequestParam(name = "brandName") String brandName) {
        Stand stand = standRepository.findStandById(standName, brandName).orElse(null);
        if (stand != null) {
            return ResponseEntity.ok(BetterResponseModel.ok( "Successfully retrieved orders from database",stand.getOrderList().stream().map(Order::asCommonOrder).collect(Collectors.toList())));
        } else{
            DoesNotExistException e= new DoesNotExistException("Stand " + standName + " does not exist, or does not have orders saved.");
            e.printStackTrace();
            return ResponseEntity.ok(BetterResponseModel.error("Error while fetching orders from database", e));
        }
    }
}


