import cobol.services.ordermanager.OrderManagerController;
import cobol.services.ordermanager.dbmenu.Order;
import cobol.services.ordermanager.dbmenu.OrderItemRepository;
import cobol.services.ordermanager.dbmenu.OrderRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileReader;
import java.net.URL;

@SpringBootTest
@ComponentScan(basePackages = "cobol.services.ordermanager.dbmenu")
@EnableJpaRepositories(basePackages = {"cobol.services.ordermanager.dbmenu"})
public class OrderManagerTest {

    @Autowired
    OrderRepository orders;

    @Test
    public void saveOrder() throws Exception{
        JSONParser parser= new JSONParser();
        try{
            // Create JSONObject
            URL url=Thread.currentThread().getContextClassLoader().getResource("orderrequest.json");
            Object obj= parser.parse(new FileReader(url.getPath()));
            JSONObject orderFile=(JSONObject) obj;


        } catch(Exception e){
            e.printStackTrace();
        }


    }

}
