package cobol.services.ordermanager.controller;

import cobol.services.ordermanager.domain.entity.Brand;
import cobol.services.ordermanager.domain.entity.Category;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.BrandRepository;
import cobol.services.ordermanager.domain.repository.CategoryRepository;
import cobol.services.ordermanager.domain.repository.FoodRepository;
import cobol.services.ordermanager.domain.repository.StandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/db")
@RestController
public class DBController {

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    StandRepository standRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    FoodRepository foodRepository;

    @GetMapping("/load")
    public ResponseEntity load(@RequestBody List<Brand> data){
        data.forEach(brandRepository::saveAndFlush);
        return null;
    }




    @GetMapping("/clear")
    public ResponseEntity clear(){

        brandRepository.deleteAll();

        return null;
    }

    @GetMapping("/export")
    public ResponseEntity export(){
        List<Brand> brands= brandRepository.findAll();
        return ResponseEntity.ok(brands);
    }
}
