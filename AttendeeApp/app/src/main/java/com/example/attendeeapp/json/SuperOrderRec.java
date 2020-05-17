package com.example.attendeeapp.json;


import java.util.List;

/**
 * Model for the server answer when placing a (super) order ad requesting recommendations.
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
