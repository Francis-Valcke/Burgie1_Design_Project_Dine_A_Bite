package com.example.attendeeapp.json;

public class Recommendation {

    // standId of recommendation
    private String standName;
    private String brandName;

    // meters
    private double distance;
    // milliseconds
    private int timeEstimate;

    private int rank;


    public Recommendation() {
    }

    public Recommendation(String standName, String brandName, double distance, int timeEstimate, int rank) {
        this.standName=standName;
        this.brandName=brandName;
        this.distance = distance;
        this.timeEstimate = timeEstimate;
        this.rank = rank;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getStandName() {
        return standName;
    }

    public void setStandName(String standName) {
        this.standName = standName;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
