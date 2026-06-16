package com.marcos.fisikappmovil.model;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREF_NAME = "fisikapp_auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public boolean hasSession() {
        String accessToken = getAccessToken();
        return accessToken != null && !accessToken.trim().isEmpty();
    }

    public String getAuthorizationHeader() {
        String accessToken = getAccessToken();

        if (accessToken == null || accessToken.trim().isEmpty()) {
            return null;
        }

        return "Bearer " + accessToken;
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}