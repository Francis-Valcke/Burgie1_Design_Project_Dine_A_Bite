package com.example.standapp;

public class ServerConfig {

    private static final boolean localTest = true;
    private static final boolean emulator= true;

    private static final String ip = emulator ? "10.0.2.2" : "192.168.0.221";
    public static final String OM_ADDRESS = localTest ? "http://"+ip+":8081" : "http://cobol.idlab.ugent.be:8091";
    public static final String EC_ADDRESS = localTest ? "http://"+ip+":8083" : "http://cobol.idlab.ugent.be:8093";
    public static final String AS_ADDRESS = localTest ? "http://"+ip+":8080" : "http://cobol.idlab.ugent.be:8090";

}
