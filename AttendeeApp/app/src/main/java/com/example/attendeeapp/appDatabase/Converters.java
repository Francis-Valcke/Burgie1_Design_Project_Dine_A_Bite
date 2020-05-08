package com.example.attendeeapp.appDatabase;

import androidx.room.TypeConverter;

import com.example.attendeeapp.json.CommonOrder;
import com.example.attendeeapp.json.CommonOrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

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
    public static Integer getRecommendTypeInt(CommonOrder.RecommendType type) {
        return type.ordinal();
    }

    @TypeConverter
    public static CommonOrder.RecommendType getRecommendType(Integer type) {
        return CommonOrder.RecommendType.values()[type];
    }

    @TypeConverter
    public static Long ZonedDateTimeToTimestamp(ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : zonedDateTime.toInstant().toEpochMilli();
    }

    @TypeConverter
    public static ZonedDateTime ZonedDateTimefromTimestamp(Long value) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.of("Europe/Brussels"));
    }

    @TypeConverter
    public static CommonOrder.State getStatus(Integer numeral){
        return CommonOrder.State.values()[numeral];
    }

    @TypeConverter
    public static Integer getStatusInt(CommonOrder.State state){
        return state.ordinal();
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
