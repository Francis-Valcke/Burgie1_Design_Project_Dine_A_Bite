package cobol.commons;

public class BetterResponseModel<T> {

    private String status;
    private T details;

    public BetterResponseModel() {
    }

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

    public static class GetBalanceResponse {

        private double balance;

        public GetBalanceResponse() {
        }

        public GetBalanceResponse(double balance) {
            this.balance = balance;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }
}

