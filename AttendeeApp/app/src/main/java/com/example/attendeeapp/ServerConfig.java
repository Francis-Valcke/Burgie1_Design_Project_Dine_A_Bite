package com.example.attendeeapp;

public class ServerConfig {

    private static final boolean localTest = false;
    public static final String OM_ADDRESS = localTest ? "http://192.168.1.43:8081" : "http://cobol.idlab.ugent.be:8091";
    public static final String EC_ADDRESS = localTest ? "http://192.168.1.43:8083" : "http://cobol.idlab.ugent.be:8093";
    public static final String AS_ADDRESS = localTest ? "http://192.168.1.43:8080" : "http://cobol.idlab.ugent.be:8090";

}

