package cobol.services.commons.test;

import cobol.commons.order.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import java.io.FileReader;
import java.net.URL;
import java.sql.PseudoColumnUsage;

public class CommonsTest {


    @Test
    public void orderRequest() throws Exception {
        JSONParser parser= new JSONParser();
        try{
            // Create JSONObject
            URL url=Thread.currentThread().getContextClassLoader().getResource("orderrequest.json");
            Object obj= parser.parse(new FileReader(url.getPath()));
            JSONObject orderFile=(JSONObject) obj;



            // Make order object
            ObjectMapper mapper = new ObjectMapper();
            Order order= mapper.readValue(orderFile.toJSONString(), Order.class);
            System.out.println(order.toString());

        } catch(Exception e){
            e.printStackTrace();
        }


    }

}
