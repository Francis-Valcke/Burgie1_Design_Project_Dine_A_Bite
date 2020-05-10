package cobol.commons.order;

public class Recommendation {

    // standId of recommendation
    private String standName;
    private String brandName;

    // meters
    private double distance;
    // milliseconds
    private int timeEstimate;
    // the rank of the recommendation (1 meaning it's the best recomendation etc)
    private int rank;

    //ID of scheduler where this recommend was coming from (used in priority queues)
    private int schedulerId;
    //preparation time of the order in the scheduler of the recommendation (used in priority queues, necessary if different prep times for same order in different stands)
    private int orderPrepTime;

    public Recommendation() {
    }

    public Recommendation(String standName, String brandName, double distance, int timeEstimate, int rank, int schedulerId, int prepTime) {
        this.standName=standName;
        this.brandName=brandName;
        this.distance = distance;
        this.timeEstimate = timeEstimate;
        this.rank = rank;
        this.schedulerId = schedulerId;
        this.orderPrepTime = prepTime;
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

    public int getSchedulerId() {
        return schedulerId;
    }

    public int getOrderPrepTime() {
        return orderPrepTime;
    }
}
