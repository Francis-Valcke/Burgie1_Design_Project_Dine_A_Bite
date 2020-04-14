package com.example.standapp.data;

import android.util.Log;

import com.example.standapp.ServerConfig;
import com.example.standapp.data.model.LoggedInUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private volatile JSONObject jsonResponse;

    // only one client, singleton,
    // multiple instances will create more memory.
    private final OkHttpClient httpClient = new OkHttpClient();

    public Result<LoggedInUser> login(String username, String password) {

        // Now the user is stored in memory, not in cache

        // Requests with OkHttp3 library
        // Asynchronous GET request to server (pingOM)
        // (synchronous GET request runs on UI thread -> not allowed)

        // Create user POST request to Authentication service
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RequestBody body = RequestBody.create(jsonObject.toString(),
                MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(ServerConfig.AS_ADDRESS + "/createStandManager")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    if (responseBody != null) System.out.println(responseBody.string());
                    Log.d("LoginDataSource", "/createUser response");

                    // Authenticate user POST request to Authentication service
                    Request request2 = new Request.Builder()
                            .url(ServerConfig.AS_ADDRESS + "/authenticate")
                            .post(body)
                            .build();

                    httpClient.newCall(request2).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) {
                            try (ResponseBody responseBody2 = response.body()) {
                                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                                if (responseBody2 != null) System.out.println(responseBody2.string());
                                Log.d("LoginDataSource", "/createUser response");

                                if (responseBody2 != null) jsonResponse = new JSONObject(responseBody2.string());
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Wait for responses from Authentication service
        while(jsonResponse == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Waiting for response from server...");
        }

        // Create new user or return error
        try {
            // TODO: handle loggedInUser authentication
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }

    }

    void logout() {
        // revoke authentication (not implemented in authentication service)
    }

}
