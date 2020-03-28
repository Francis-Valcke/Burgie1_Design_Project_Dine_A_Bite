package com.example.attendeeapp.order;

public class Recommendation {

    // standId of recommendation
    private int standId;
    private String standName;


    // meters
    private double distance;
    // milliseconds
    private int timeEstimate;


    public Recommendation() {
    }

    public Recommendation(int standId,String standName, double distance, int timeEstimate) {
        this.standId = standId;
        this.standName=standName;
        this.distance = distance;
        this.timeEstimate = timeEstimate;
    }

    public String getStandName() {
        return standName;
    }

    public void setStandName(String standName) {
        this.standName = standName;
    }

    public int getStandId() {
        return standId;
    }

    public void setStandId(int standId) {
        this.standId = standId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getTimeEstimate() {
        return timeEstimate;
    }

    public void setTimeEstimate(int timeEstimate) {
        this.timeEstimate = timeEstimate;
    }
}
