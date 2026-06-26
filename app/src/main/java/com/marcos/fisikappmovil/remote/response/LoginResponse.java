package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;
import com.marcos.fisikappmovil.model.UserData;

public class LoginResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("access")
    private String accessToken;

    @SerializedName("refresh")
    private String refreshToken;

    @SerializedName("user")
    private UserData user;

    public String getMessage() {
        return message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UserData getUser() {
        return user;
    }

    public boolean hasValidAccessToken() {
        return accessToken != null && !accessToken.trim().isEmpty();
    }

    public boolean hasValidRefreshToken() {
        return refreshToken != null && !refreshToken.trim().isEmpty();
    }
}