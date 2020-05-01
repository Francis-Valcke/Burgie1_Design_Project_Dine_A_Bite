package com.example.attendeeapp.json;


public class BetterResponseModel<T> {

    String status;
    T details;

    public BetterResponseModel(String status, T details) {
        this.status = status;
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getDetails() {
        return details;
    }

    public void setDetails(T details) {
        this.details = details;
    }

    public static class Status{
        public static final String OK = "OK";
        public static final String ERROR = "ERROR";
    }

    public static class CreatePaymentIntentResponse {

        public CreatePaymentIntentResponse(String clientSecret, String publicKey) {
            this.clientSecret = clientSecret;
            this.publicKey = publicKey;
        }

        String clientSecret;
        String publicKey;

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }
    }
}
