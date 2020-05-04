package com.example.attendeeapp;

public class ServerConfig {

//    public static final String OM_ADDRESS = "http://10.0.2.2:8081";

    private static final boolean localTest = true;
    public static final String OM_ADDRESS = localTest ? "http://192.168.1.43:8081" : "http://cobol.idlab.ugent.be:8091";
    public static final String EC_ADDRESS = localTest ? "http://192.168.1.43:8083" : "http://cobol.idlab.ugent.be:8093";
    public static final String AS_ADDRESS = "http://cobol.idlab.ugent.be:8090";

}
