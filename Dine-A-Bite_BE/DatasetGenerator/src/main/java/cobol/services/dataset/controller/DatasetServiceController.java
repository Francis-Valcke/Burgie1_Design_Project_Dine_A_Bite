package cobol.services.dataset.controller;

import cobol.services.dataset.domain.entity.Brand;
import cobol.services.dataset.domain.entity.Category;
import cobol.services.dataset.domain.entity.Stand;
import cobol.services.dataset.domain.repository.BrandRepository;
import cobol.services.dataset.domain.repository.CategoryRepository;
import cobol.services.dataset.domain.repository.FoodRepository;
import cobol.services.dataset.domain.repository.StandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DatasetServiceController {

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

        Category category = categoryRepository.findById("SNACK").get();

        List<Stand> stand = standRepository.findAll();

        return null;



    }

    @GetMapping("/clear")
    public ResponseEntity clear(){

        brandRepository.deleteAll();

        return null;
    }
}
