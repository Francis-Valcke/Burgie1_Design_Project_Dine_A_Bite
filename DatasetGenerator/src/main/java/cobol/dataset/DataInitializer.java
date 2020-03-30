package cobol.dataset;

import cobol.dataset.domain.entity.Brand;
import cobol.dataset.domain.entity.Food;
import cobol.dataset.domain.entity.Stand;
import cobol.dataset.domain.entity.Stock;
import cobol.dataset.domain.repository.BrandRepository;
import cobol.dataset.domain.repository.FoodRepository;
import cobol.dataset.domain.repository.StandRepository;
import cobol.dataset.domain.repository.StockRepository;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    StockRepository stockRepository;
    /**
     * Debug methods for placing some initial users into the database.
     * @param args runtime arguments
     * @throws Exception throw any kind of exception
     */
    @Override
    public void run(String... args) throws Exception {

        List<Brand> dataset = Dataset1.getDataset1();

        for (Brand brand : dataset) {

            for (Food food : brand.getFoodList()) {
                food.setBrand(brand);
            }

            for (Stand stand : brand.getStandList()) {
                stand.setBrand(brand);
            }

            brandRepository.saveAndFlush(brand);
        }

        List<Brand> brands = brandRepository.findAll();

        for (Brand brand : brands) {
            for (Stand stand : brand.getStandList()) {
                for (Food food : brand.getFoodList()) {
                    Stock stock = new Stock(food.getId(), stand.getId(), 25);
                    stockRepository.saveAndFlush(stock);
                }
            }
        }


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
    public void setStockRepository(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Autowired
    public void setTm(PlatformTransactionManager tm) {
        this.tm = tm;
    }
}
