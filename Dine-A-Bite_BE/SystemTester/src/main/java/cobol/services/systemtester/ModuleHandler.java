package cobol.services.systemtester;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

public class ModuleHandler {
    public boolean pingOM() {
        try{
            JSONObject responseBody =  Unirest.get(ServerConfig.OMURL + "/pingOM")
                    .header("Content-Type", "application/json")
                    .asJson()
                    .getBody()
                    .getObject();
            if (responseBody.get("status").equals("OK")) return true;
        }catch (Exception e){
            return false;
        }
        return false;
    }
    public boolean pingSM()  {
        try{
            JSONObject responseBody =  Unirest.get(ServerConfig.SMURL + "/pingSM")
                    .header("Content-Type", "application/json")
                    .asJson()
                    .getBody()
                    .getObject();
            if (responseBody.get("status").equals("OK")) return true;
        }catch (Exception e){
            return false;
        }
        return false;
    }
    public boolean pingAS()  {
        try{
            JSONObject responseBody =  Unirest.get(ServerConfig.ACURL + "/pingAS")
                    .header("Content-Type", "application/json")
                    .asJson()
                    .getBody()
                    .getObject();
            if (responseBody.get("status").equals("OK")) return true;
        }catch (Exception e){
            return false;
        }
        return false;
    }
    public boolean pingEC()  {
        try{
            JSONObject responseBody =  Unirest.get(ServerConfig.ECURL + "/pingEC")
                    .header("Content-Type", "application/json")
                    .asJson()
                    .getBody()
                    .getObject();
            if (responseBody.get("status").equals("OK")) return true;
        }catch (Exception e){
            return false;
        }
        return false;
    }

    /**
     * checks if all backend modules are alive
     * @return
     */
    public boolean allAlive()  {
        boolean b= false;
        while(!b){
            b=pingOM()&&pingAS()&&pingSM()&&pingEC();
        }
        return true;
    }
}
