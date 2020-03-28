package cobol.dataset;

import cobol.dataset.domain.entity.Food;
import cobol.dataset.domain.entity.Stand;
import cobol.dataset.domain.repository.StandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * This class will be discovered by the component scanner and exposed as a bean on which the run method will be run.
 * In this run method some initial values are added to the database.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    StandRepository standRepository;

    /**
     * Debug methods for placing some initial users into the database.
     * @param args runtime arguments
     * @throws Exception throw any kind of exception
     */
    @Override
    public void run(String... args) throws Exception {

        Dataset1.getDataset1().forEach(s -> standRepository.saveAndFlush(s));

    }

    @Autowired
    public void setStandRepository(StandRepository standRepository) {
        this.standRepository = standRepository;
    }
}
