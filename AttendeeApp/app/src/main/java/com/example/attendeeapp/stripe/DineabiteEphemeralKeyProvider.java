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
import com.example.attendeeapp.polling.OkHttpRequestTool;
import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;
import com.stripe.android.Stripe;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DineabiteEphemeralKeyProvider extends Activity implements EphemeralKeyProvider {

    private LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

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
                    ephemeralKeyUpdateListener.onKeyUpdate(Objects.requireNonNull(response));
                },
                throwable -> {
                    Log.e("STRIPE", "createEphemeralKey: ", throwable);
                }
        );

    }
}
