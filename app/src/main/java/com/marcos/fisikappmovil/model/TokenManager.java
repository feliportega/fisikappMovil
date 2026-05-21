package com.marcos.fisikappmovil.model;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private SharedPreferences prefs;
    public TokenManager(Context context) {
        prefs = context.getSharedPreferences("token", Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString("jwt_token", token).apply();
    }

    public String getToken() {
        return prefs.getString("jwt_token", null);
    }

    public void clearToken() {
        prefs.edit().remove("jwt_token").apply();
    }
}
