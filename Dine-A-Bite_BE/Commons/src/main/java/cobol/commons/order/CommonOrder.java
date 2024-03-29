package cobol.commons.order;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;

//import java.util.Calendar;

/**
 * Order class used to send orders between the different components of the system
 */

@Data
public class CommonOrder {

    private int id;

    @JsonSerialize(using= ZonedDateTimeSerializer.class)
    private ZonedDateTime startTime;
    @JsonSerialize(using= ZonedDateTimeSerializer.class)
    private ZonedDateTime expectedTime;
    private State orderState;
    private String brandName;
    private String standName;
    private RecommendType recType;
    private int totalCount;
    private BigDecimal totalPrice;

    private List<CommonOrderItem> orderItems;

    // Coordinates attendee on moment that order was made
    private double latitude;
    private double longitude;


    public CommonOrder() {
    }

    public CommonOrder(int id, ZonedDateTime startTime, ZonedDateTime expectedTime, State orderState, String brandName, String standName, List<CommonOrderItem> orderItems, double latitude, double longitude, RecommendType recType, int totalCount, BigDecimal totalPrice) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Brussels"));
        this.id = id;
        this.startTime = startTime;
        this.expectedTime = expectedTime;
        this.orderState = orderState;
        this.brandName = brandName;
        this.standName = standName;
        this.orderItems = orderItems;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recType = recType;
        this.totalCount = totalCount;
        this.totalPrice = totalPrice;
    }

    /**
     * State of the order
     */
    public enum State {
        SEND,
        PENDING,
        DECLINED,
        CONFIRMED,
        READY,
        PICKED_UP,
        BEGUN
    }

    /**
     * Type of recommendation the attendee wants
     */
    public enum RecommendType {
        DISTANCE,
        TIME,
        DISTANCE_AND_TIME
    }

    /**
     * Computes remaining time till expected time with respect to current time
     *
     * @return RemainingTime in seconds
     */
    public int computeRemainingTime() {
        ZonedDateTime actual = ZonedDateTime.now(ZoneId.of("Europe/Brussels"));
        if (actual.isAfter(expectedTime)) {
            return -1;
        } else {
            Duration remaining = Duration.between(actual, expectedTime);
            return (int) remaining.getSeconds();
        }
    }


}
