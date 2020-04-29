package com.example.attendeeapp.stripe;

import android.app.Activity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendeeapp.ServerConfig;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.polling.RequestQueueSingleton;
import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;
import com.stripe.android.Stripe;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DineabiteEphemeralKeyProvider extends Activity implements EphemeralKeyProvider {

    private LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

    @Override
    public void createEphemeralKey(@NotNull String apiVersion, @NotNull EphemeralKeyUpdateListener ephemeralKeyUpdateListener) {

        String url = ServerConfig.AS_ADDRESS + "/stripe/key?api_version="+apiVersion;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.i("STRIPE",response.toString() );
                    ephemeralKeyUpdateListener.onKeyUpdate(response.toString());
                },
                error -> {
                    Log.e("STRIPE", "createEphemeralKey: ", error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  headers  = new HashMap<>();
                headers.put("Authorization", user.getAuthorizationToken());
                return headers;
            }
        };

        RequestQueueSingleton.getInstance().addToRequestQueue(request);
    }
}
