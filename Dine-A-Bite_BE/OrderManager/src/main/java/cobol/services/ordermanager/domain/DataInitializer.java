package cobol.services.ordermanager.domain;

import cobol.services.ordermanager.domain.entity.Food;
import cobol.services.ordermanager.domain.entity.Stand;
import cobol.services.ordermanager.domain.repository.StandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * This class will be discovered by the component scanner and exposed as a bean on which the run method will be run.
 * In this run method some initial values are added to the database.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    StandRepository standRepository;

    /**
     * Debug methods for placing some initial users into the database.
     * @param args runtime arguments
     * @throws Exception throw any kind of exception
     */
    @Override
    public void run(String... args) throws Exception {

        Food food1 = new Food("bacon", "baconator",25);
        Food food2 = new Food("bacon2", "baconator",25);
        Food food3 = new Food("bacon3", "baconator",25);
        List<Food> food = Arrays.asList(food1, food2, food3);

        Stand stand = new Stand("BACONNNN", "baconator", 25.0, 25,food);

        standRepository.saveAndFlush(stand);

        Stand newStand = standRepository.findById(stand.getId()).get();
        System.out.println();

    }


}
