package cobol.services.dataset.controller;

import cobol.commons.ResponseModel;
import cobol.services.dataset.dataset.Dataset1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatasetServiceController {

    @Autowired
    Dataset1 dataset1;

    @GetMapping("/load")
    public ResponseEntity load(){

        try {

            dataset1.load();

            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .details("Dataset has been loaded")
                            .status("OK")
                            .build()
                            .generateResponse()
            );

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .details(e.getStackTrace())
                            .status("ERROR")
                            .build()
                            .generateResponse()
            );
        }

    }

    @GetMapping("/clear")
    public ResponseEntity clear(){

        try {

            dataset1.clear();

            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .details("Dataset has been cleared")
                            .status("OK")
                            .build()
                            .generateResponse()
            );

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.ok(
                    ResponseModel.builder()
                            .details(e.getStackTrace())
                            .status("ERROR")
                            .build()
                            .generateResponse()
            );
        }

    }
}
