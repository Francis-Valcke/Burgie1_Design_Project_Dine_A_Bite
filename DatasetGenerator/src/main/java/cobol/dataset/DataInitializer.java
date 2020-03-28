package cobol.dataset;

import cobol.dataset.domain.entity.Brand;
import cobol.dataset.domain.entity.Food;
import cobol.dataset.domain.entity.Stand;
import cobol.dataset.domain.entity.Stock;
import cobol.dataset.domain.repository.BrandRepository;
import cobol.dataset.domain.repository.FoodRepository;
import cobol.dataset.domain.repository.StandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Collections;

/**
 * This class will be discovered by the component scanner and exposed as a bean on which the run method will be run.
 * In this run method some initial values are added to the database.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    PlatformTransactionManager tm;


    StandRepository standRepository;
    BrandRepository brandRepository;
    FoodRepository foodRepository;
    /**
     * Debug methods for placing some initial users into the database.
     * @param args runtime arguments
     * @throws Exception throw any kind of exception
     */
    @Override
    public void run(String... args) throws Exception {

        Brand brand = Brand.builder().name("test").build();
        Stand stand = Stand.builder().name("test").brand(brand).build();
        Food food = Food.builder().name("test").brand(brand).build();

        brand.addStand(stand);
        brand.addFood(food);
        food.addStand(stand);

        //Stock stock = new Stock(food, stand, 25);
        //
        //brand.getFoodList().get(0).getStockMap().put(stand, stock);

        brand = brandRepository.save(brand);



        //Brand newBrand = brandRepository.findAll().get(0);
        //System.out.println();
    }

    @Autowired
    public void setStandRepository(StandRepository standRepository) {
        this.standRepository = standRepository;
    }

    @Autowired
    public void setBrandRepository(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Autowired
    public void setFoodRepository(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @Autowired
    public void setTm(PlatformTransactionManager tm) {
        this.tm = tm;
    }
}
