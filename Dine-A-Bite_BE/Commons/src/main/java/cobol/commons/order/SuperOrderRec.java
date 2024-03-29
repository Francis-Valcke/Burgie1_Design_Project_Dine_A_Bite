package cobol.commons.order;

import java.util.List;


/**
 * This class is used to hold together an order with his recommendations
 * In this way, it is easy to send an object of this type to the AttendeeApp
 */
public class SuperOrderRec {

    private CommonOrder order;
    private List<Recommendation> recommendations;


    public SuperOrderRec(CommonOrder order, List<Recommendation> recommendations) {
        this.order = order;
        this.recommendations = recommendations;
    }

    public SuperOrderRec() {
    }

    public CommonOrder getOrder() {
        return order;
    }

    public void setOrder(CommonOrder order) {
        this.order = order;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }
}
