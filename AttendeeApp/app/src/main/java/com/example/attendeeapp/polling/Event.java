package com.example.attendeeapp.polling;

import com.fasterxml.jackson.databind.JsonNode;


import java.util.List;

/**
 * Model for an event used in the EventChannel.
 */
public class Event {


        private static int idCount = 0;
        private int myId;
        private String dataType;
        protected JsonNode eventData;
        private List<String> types;

        public Event() {
            myId = idCount;
            idCount++;
            eventData = null;
        }


        public Event(JsonNode data, List<String> types, String dataType) {
            myId = idCount;
            idCount++;
            eventData = data;
            this.types = types;
            this.dataType = dataType;
        }

        public JsonNode getEventData() {
            return this.eventData;
        }

        public List<String> getTypes() {
            return types;
        }

        public int getMyId() {
            return myId;
        }

        public void setTypes(List<String> newTypes) {
            types = newTypes;
        }

        public String getDataType() {
            return dataType;
        }


}
