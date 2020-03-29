import cobol.services.ordermanager.domain.repository.OrderRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
