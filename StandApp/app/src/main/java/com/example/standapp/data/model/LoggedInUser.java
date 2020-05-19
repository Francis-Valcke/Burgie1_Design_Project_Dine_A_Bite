package com.example.standapp.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId; // authentication token
    private String displayName;

    public LoggedInUser(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the authorization token for the user to be used in HTTP requests to the server
     *
     * @return authorization token as String value with "Bearer" at the beginning
     */
    public String getAuthorizationToken() {
        return "Bearer " + getUserId();
    }
}
