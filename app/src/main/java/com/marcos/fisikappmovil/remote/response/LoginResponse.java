package com.marcos.fisikappmovil.remote.response;

import com.marcos.fisikappmovil.model.UserData;

public class LoginResponse {
    private String message;
    private String access;
    private String refresh;
    private UserData user;

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return access;
    }

    public String getRefresh() {
        return refresh;
    }

    public UserData getUser() {
        return user;
    }
}
