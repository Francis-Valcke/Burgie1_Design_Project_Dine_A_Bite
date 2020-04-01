package cobol.commons.order;

public class Recommendation {

    // standId of recommendation
    private String standName;
    private String brandName;

    // meters
    private double distance;
    // milliseconds
    private int timeEstimate;


    public Recommendation() {
    }

    public Recommendation(String standName, String brandName, double distance, int timeEstimate) {
        this.standName=standName;
        this.brandName=brandName;
        this.distance = distance;
        this.timeEstimate = timeEstimate;
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
}
