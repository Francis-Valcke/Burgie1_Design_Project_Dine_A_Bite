package com.example.attendeeapp.json;

import java.math.BigDecimal;

/**
 * This class is used for wrapping responses with other information.
 * When the status of the response is OK, it will contain a payload of type T together with details about the payload.
 * When the status of the response is ERROR, the payload can be empty but a exception is expected together with details
 * about what happened.
 * @param <T> The type of the payload in case of status OK.
 */
public class BetterResponseModel<T> {

    private String status;
    private String details;
    private T payload;
    private Throwable exception;

    /**
     * Convenience method for creating BetterResponseModel with status OK.
     * @param details Details about the payload.
     * @param payload The actual payload object.
     * @param <T> The type of the payload object.
     * @return Instance of BetterResponseModel with payload of type T
     */
    public static <T> BetterResponseModel<T> ok(String details, T payload){
        return new BetterResponseModel<T>(details, payload);
    }

    /**
     * Convenience method for creating BetterResponseModel with status OK.
     * @param details Details the exception that was thrown.
     * @param e The exception that was thrown.
     * @param <T> The type of the payload object.
     * @return Instance of BetterResponseModel with payload of type T
     */
    public static <T> BetterResponseModel<T> error(String details, Throwable e){
        return new BetterResponseModel<T>(details, e);
    }

    public BetterResponseModel() {
    }

    public BetterResponseModel(String status, String details, T payload, Throwable exception) {
        this.status = status;
        this.details = details;
        this.payload = payload;
        this.exception = exception;
    }

    private BetterResponseModel(String details, Throwable exception) {
        this.status = Status.ERROR;
        this.details = details;
        this.exception = exception;
        this.payload = null;
    }

    private BetterResponseModel(String details, T payload) {
        this.status = Status.OK;
        this.details = details;
        this.exception = null;
        this.payload = payload;
    }

    public boolean isOk(){
        return status.equals(Status.OK);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public static class Status{
        public static final String OK = "OK";
        public static final String ERROR = "ERROR";
    }

    // ---- Implement DATA objects here ----

    /**
     * Data class to represent response when creating a payment intent.
     */
    public static class CreatePaymentIntentResponse {

        private String clientSecret;
        private String publicKey;

        public CreatePaymentIntentResponse() {
        }

        public CreatePaymentIntentResponse(String clientSecret, String publicKey) {
            this.clientSecret = clientSecret;
            this.publicKey = publicKey;
        }

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

    /**
     * Data class to represent response when balance is requested.
     */
    public static class GetBalanceResponse {

        private BigDecimal balance;

        public GetBalanceResponse() {
        }

        public GetBalanceResponse(BigDecimal balance) {
            this.balance = balance;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
    }

}

