package cobol.services.standmanager;
import cobol.services.standmanager.dbmenu.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class StandManager {

    public static void main(String[] args) throws InterruptedException, SQLException {
        SpringApplication.run(StandManager.class,args);



        //test
        Food f1 = new Food("apple", 10, 4, null, "fruit");
        Food f2 = new Food("burger", 15,  10, "bacon between bread", "american");
        Food f3 = new Food("pizza", 20,  15);
        Food f4 = new Food("pizza with salami", 20,  16, "salami on pizza, Duhh", "italian");
        ArrayList<Food> menu =new ArrayList<Food>();
        menu.add(f1);
        menu.add(f2);
        menu.add(f3);
        menu.add(f4);
        int[] l = {2, 4, 7, 3};

    }

}