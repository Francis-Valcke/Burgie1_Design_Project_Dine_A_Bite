package cobol.services.dataset.controller;

import cobol.commons.ResponseModel;
import cobol.services.dataset.dataset.Dataset1;
import cobol.services.dataset.domain.entity.Brand;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DatasetServiceController {

    @Autowired
    Dataset1 dataset1;

    @GetMapping("/load")
    public ResponseEntity load(@RequestBody List<Brand> data){

        System.out.println("test");

        return null;

    }

    @GetMapping("/clear")
    public ResponseEntity clear(){

        //try {
        //
        //    dataset1.clear();
        //
        //    return ResponseEntity.ok(
        //            ResponseModel.builder()
        //                    .details("Dataset has been cleared")
        //                    .status("OK")
        //                    .build()
        //                    .generateResponse()
        //    );
        //
        //} catch (Exception e) {
        //    e.printStackTrace();
        //
        //    return ResponseEntity.ok(
        //            ResponseModel.builder()
        //                    .details(e.getStackTrace())
        //                    .status("ERROR")
        //                    .build()
        //                    .generateResponse()
        //    );
        //}

        return null;
    }
}
