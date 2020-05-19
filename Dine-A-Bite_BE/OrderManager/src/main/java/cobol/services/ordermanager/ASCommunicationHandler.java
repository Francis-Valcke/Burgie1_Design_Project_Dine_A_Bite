package cobol.services.ordermanager;

import cobol.commons.BetterResponseModel;
import cobol.commons.BetterResponseModel.GetBalanceResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ASCommunicationHandler {

    /**
     * This method sends a HTTP request to the Authentication Service module to create a transaction for a given amount
     * and a given user. This transaction will be held until confirmed.
     * @param username Username of the user to create the transaction for.
     * @param amount Amount to add or subtract to the balance of the user.
     * @return GetBalanceResponse
     * @throws IOException Exception thrown while parsing HTTP response
     */
    public BetterResponseModel<BetterResponseModel.GetBalanceResponse> callCreateTransaction(String username, BigDecimal amount) throws IOException {

        String url = OrderManager.ACURL + "/stripe/createTransaction";

        HashMap<String, String> params = new HashMap<>();
        params.put("user", username);
        params.put("amount", amount.toString());

        url = buildUrl(url, params);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("", mediaType);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Authorization", OrderManager.authToken)
                .build();
        Response response = client.newCall(request).execute();

        ObjectMapper om = new ObjectMapper();
        return om.readValue(Objects.requireNonNull(response.body()).string(), new TypeReference<BetterResponseModel<GetBalanceResponse>>() {});
    }

    /**
     * This method sends a HTTP request to the Authentication Service module to confirm a transaction for a given user.
     *
     * @param username Username of the user to confirm the transaction for.
     * @return GetBalanceResponse
     * @throws IOException Exception thrown while parsing HTTP response
     */
    public BetterResponseModel<GetBalanceResponse> callConfirmTransaction(String username) throws IOException {
        String url = OrderManager.ACURL + "/stripe/confirmTransaction";

        HashMap<String, String> params = new HashMap<>();
        params.put("user", username);

        url = buildUrl(url, params);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", OrderManager.authToken)
                .build();
        Response response = client.newCall(request).execute();

        ObjectMapper om = new ObjectMapper();
        return om.readValue(Objects.requireNonNull(response.body()).string(), new TypeReference<BetterResponseModel<GetBalanceResponse>>() {});
    }

    /**
     * Helper method to add parameters to a given URL.
     *
     * @param baseUrl baseURL.
     * @param params parameters for the http request.
     * @return url.
     */
    public String buildUrl(String baseUrl, HashMap<String, String> params) {

        if (params != null && !params.isEmpty()) {
            baseUrl += "?";
            StringBuilder baseUrlBuilder = new StringBuilder(baseUrl);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                baseUrlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            baseUrl = baseUrlBuilder.toString();
        }
        //remove the last &
        return baseUrl.substring(0, baseUrl.length() - 1);
    }
}
