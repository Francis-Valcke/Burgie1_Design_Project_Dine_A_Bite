package com.example.attendeeapp.polling;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpRequestTool {

    private static OkHttpClient client = new OkHttpClient().newBuilder().build();

    public static Single<String> wrapRequest(Request request){

        return Single.create(emitter -> {
            try {

                Response httpResponse = client.newCall(request).execute();
                String body = Objects.requireNonNull(httpResponse.body()).string();
                Objects.requireNonNull(httpResponse.body()).close();
                emitter.onSuccess(body);

            } catch (Exception e) {
                emitter.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cast(String.class);
    }

    public static String buildUrl(String baseUrl, HashMap<String, String> params) {

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
