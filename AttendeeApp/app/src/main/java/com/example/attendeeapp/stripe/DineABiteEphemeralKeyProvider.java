package com.example.attendeeapp.stripe;

import android.util.Log;

import com.example.attendeeapp.ServerConfig;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.example.attendeeapp.polling.OkHttpRequestTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

/**
 * This class is used by the Stripe payment api.
 * It requires the createEphemeralKey method to contact the backend of the system, and request an
 * ephemeral key.
 */
public class DineABiteEphemeralKeyProvider implements EphemeralKeyProvider {

    private static final String TAG = DineABiteEphemeralKeyProvider.class.getSimpleName();

    private LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

    /**
     * This method will contact the backend and request an ephemeral key.
     * @param apiVersion The version of the Stripe API the app uses
     * @param ephemeralKeyUpdateListener callback
     */
    @Override
    public void createEphemeralKey(@NotNull String apiVersion, @NotNull EphemeralKeyUpdateListener ephemeralKeyUpdateListener) {

        HashMap<String,String> params = new HashMap<>();
        params.put("api_version", apiVersion);

        String url = ServerConfig.AS_ADDRESS + "/stripe/key";
        url = OkHttpRequestTool.buildUrl(url, params);

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", user.getAuthorizationToken())
                .build();

        OkHttpRequestTool.wrapRequest(request).subscribe(
                response -> {

                    try {

                        ObjectMapper om = new ObjectMapper();
                        BetterResponseModel<String> responseModel =
                                om.readValue(response, new TypeReference<BetterResponseModel<String>>() {});
                        if (responseModel.isOk()) {
                            ephemeralKeyUpdateListener.onKeyUpdate(Objects.requireNonNull(responseModel.getPayload()));
                        } else throw responseModel.getException();

                    } catch (Exception e) {
                        Log.e(TAG, "createEphemeralKey: ", e);
                    }

                },
                throwable -> Log.e(TAG, "createEphemeralKey: ", throwable)
        );

    }
}
