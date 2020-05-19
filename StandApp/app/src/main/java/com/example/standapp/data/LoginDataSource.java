package com.example.standapp.data;

import android.util.Log;

import com.example.standapp.ServerConfig;
import com.example.standapp.data.model.LoggedInUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private volatile String mResponseBody = null;

    // only one client, singleton,
    // multiple instances will create more memory.
    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * Log in stand manager user
     * - Authenticate with server
     *
     * @param username username
     * @param password password
     * @return         logged in user object if success, error if not (#Result)
     */
    @SuppressWarnings("unchecked")
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
            return new Result.Error(new IOException("Error logging in", e));
        }

        final RequestBody body = RequestBody.create(jsonObject.toString(),
                MediaType.parse("application/json; charset=utf-8"));

        // Authenticate user POST request to Authentication service
        Request request = new Request.Builder()
                .url(ServerConfig.AS_ADDRESS + "/authenticate")
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
                    Log.d("LoginDataSource", "/authenticate response");

                    // ResponseBody can only be accessed once
                    if (responseBody != null) mResponseBody = responseBody.string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Wait for responses from Authentication service
        int interruptCounter = 0;
        while(mResponseBody == null) {
            interruptCounter++;
            try {
                Thread.sleep(500);
                if (interruptCounter >= 20) {
                    System.out.println("No response from server after 10 seconds"); // DEBUG
                    throw new InterruptedException("No response from server");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return new Result.Error(new IOException("Error logging in", e));
            }
            System.out.println("Waiting for response from server..."); // DEBUG
        }

        // Getting the information from the authentication response
        ObjectMapper mapper = new ObjectMapper();
        String token;
        String status;
        try {
            JsonNode jsonNode = mapper.readTree(mResponseBody);
            status = jsonNode.get("status").textValue();
            if (status.equals("ERROR")) throw new IOException(status);
            token = jsonNode.get("details").get("token").textValue();
        } catch (IOException e) {
            mResponseBody = null;
            e.printStackTrace();
            return new Result.Error(new IOException("Error logging in", e));
        }

        // Create new LoggedInUser and save in LoginRepository
        mResponseBody = null;
        LoggedInUser user = new LoggedInUser(token, username);
        return new Result.Success<>(user);
    }

    /**
     * Register new stand manager user
     * - Create user
     * - Authenticate with server
     *
     * @param username username
     * @param password password
     * @param email    email
     * @return         logged in user object if success, error if not (#Result)
     */
    @SuppressWarnings({"unchecked", "unused"})
    Result<LoggedInUser> register(String username, String password, String email) {

        // Now the user is stored in memory, not in cache

        // Email not used for the moment

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
            return new Result.Error(new IOException("Error logging in", e));
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
                    Log.d("LoginDataSource", "/createStandManager response");
                    if (responseBody != null) {
                        // ResponseBody can only be accessed once
                        // Check if the account already exists
                        // - Status == ERROR means the account already exists or something is wrong
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(responseBody.string());
                        System.out.println(jsonNode.toPrettyString()); // DEBUG
                        String status = jsonNode.get("status").textValue();
                        if (status.equals("ERROR")) throw new IOException(status);
                    }

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
                                Log.d("LoginDataSource", "/authenticate response");

                                // ResponseBody can only be accessed once
                                if (responseBody2 != null) mResponseBody = responseBody2.string();
                            } catch (IOException e) {
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
        int interruptCounter = 0;
        while(mResponseBody == null) {
            interruptCounter++;
            try {
                Thread.sleep(500);
                if (interruptCounter >= 20) {
                    System.out.println("No response from server after 10 seconds"); // DEBUG
                    throw new InterruptedException("No response from server");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return new Result.Error(new IOException("Error logging in", e));
            }
            System.out.println("Waiting for response from server..."); // DEBUG
        }

        // Getting the information from the authentication response
        ObjectMapper mapper = new ObjectMapper();
        String token;
        String status;
        try {
            JsonNode jsonNode = mapper.readTree(mResponseBody);
            status = jsonNode.get("status").textValue();
            if (status.equals("ERROR")) throw new IOException(status);
            token = jsonNode.get("details").get("token").textValue();
        } catch (IOException e) {
            mResponseBody = null;
            e.printStackTrace();
            return new Result.Error(new IOException("Error logging in", e));
        }

        // Create new LoggedInUser and save in LoginRepository
        mResponseBody = null;
        LoggedInUser user = new LoggedInUser(token, username);
        return new Result.Success<>(user);
    }

    /**
     * Log out functionality to revoke authentication of logged in user
     * (not implemented in current version of Authentication service)
     */
    void logout() {
        // revoke authentication (not implemented in authentication service)
    }

}
