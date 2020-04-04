package com.example.attendeeapp.appDatabase;

import androidx.room.TypeConverter;

import com.example.attendeeapp.order.CommonOrder;
import com.example.attendeeapp.order.CommonOrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

public class Converters {
    private static ObjectMapper mapper = new ObjectMapper();

    @TypeConverter
    public static List<CommonOrderItem> fromString(String jsonString) {
        List<CommonOrderItem> orderItems = null;
        try {
            orderItems = mapper.readValue(jsonString, new TypeReference<List<CommonOrderItem>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return orderItems;
    }

    @TypeConverter
    public static String fromArrayList(List<CommonOrderItem> list) {
        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    @TypeConverter
    public static Calendar fromTimestamp(Long value) {
        Calendar cal = Calendar.getInstance();
        if (value == null) {
            return null;
        }
        cal.setTimeInMillis(value);
        return cal;
    }

    @TypeConverter
    public static Long CalendarToTimestamp(Calendar cal) {
        return cal == null? null : cal.getTime().getTime();
    }

    @TypeConverter
    public static CommonOrder.status getStatus(Integer numeral){
        return CommonOrder.status.values()[numeral];
    }

    @TypeConverter
    public static Integer getStatusInt(CommonOrder.status status){
        return status.ordinal();
    }

    @TypeConverter
    public static BigDecimal getBigDecimal(String value){
        return new BigDecimal(value);
    }

    @TypeConverter
    public static String toString(BigDecimal value){
        return value.toString();
    }

}
