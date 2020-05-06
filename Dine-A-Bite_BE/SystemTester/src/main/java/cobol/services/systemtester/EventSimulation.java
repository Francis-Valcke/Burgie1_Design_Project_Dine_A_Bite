package cobol.services.systemtester;

import cobol.commons.CommonStand;
import cobol.services.systemtester.stage.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class EventSimulation {
    private ArrayList<Stand> stands = new ArrayList<>();
    private ArrayList<Stage> stages = new ArrayList<Stage>();
    private int stageCount = 5;
    private static final Logger log = LogManager.getLogger(EventSimulation.class);

    public EventSimulation() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("dataset.json");
        String body = Resources.toString(url, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        stands = (ArrayList<Stand>) mapper.readValue(body, new TypeReference<List<Stand>>() {
        });

    }
    public void setup(int size){
        Random ran = new Random();
        int j = 0;
        //create stages and spread stands around stages
        for (int i=0;i<stageCount;i++){
            Stage s = new Stage(0, 0, 1, 1, size/stageCount);
            if (j<stands.size())s.addStand(stands.get(j));
            j++;
            if (j<stands.size())s.addStand(stands.get(j));
            stages.add(s);
            for (Attendee a : s.getAttendees()){
                a.setOrdertime(ran.nextGaussian()*30+60);
            }

        }
        //put remaining stands around first stage
        while (j<stands.size()){
            stages.get(0).addStand(stands.get(j));
            j++;
        }
        for (Stand s:stands){
            s.setup(log);
        }
    }

    public void start(){
        for (Stand s:stands){
            s.run();
        }
        for (Stage s:stages){
            for (Attendee a:s.getAttendees()){
                a.run();//running 10k attendee threads?
            }
        }
    }
    public void end(){
        for (Stand s:stands){
            s.delete().subscribe(
                    o -> log.info("Stand " + s.getName() + " deleted"),
                    throwable -> log.error(throwable.getMessage())
            );
        }
    }

}
