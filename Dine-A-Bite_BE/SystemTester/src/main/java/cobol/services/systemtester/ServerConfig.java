package cobol.services.systemtester;

import java.util.Random;


public class ServerConfig {

    public static final boolean test = false;

    public static final String ACURL = test ? "http://localhost:8080/" : "http://cobol.idlab.ugent.be:8090/";
    public static final String OMURL = test ? "http://localhost:8081/" : "http://cobol.idlab.ugent.be:8091/";
    public static final String SMURL = test ? "http://localhost:8082/" : "http://cobol.idlab.ugent.be:8092/";
    public static final String ECURL = test ? "http://localhost:8083/" : "http://cobol.idlab.ugent.be:8093/";

    public static final double latStart = 51.031652;
    public static final double lonStart = 3.782850;
    public static final int attendeeCount = 20;
    public static final double totaltestseconds=40.0;//minutes irl
    

}
