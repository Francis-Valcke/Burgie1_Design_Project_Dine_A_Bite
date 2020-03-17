package cobol.commons;

import lombok.Builder;

import java.util.HashMap;

@Builder
public class ResponseModel {

    private Object status;
    private Object details;

    public HashMap<Object, Object> generateResponse(){
        HashMap<Object, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("details", details);
        return map;
    }


    public enum status{
        OK, ERROR
    }

}
