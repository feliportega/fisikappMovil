package com.marcos.fisikappmovil.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class TokenManager {

    private static final String PREF_NAME = "fisikapp_auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private final SharedPreferences prefs;

    public void saveUserData(String name, String email, String role) {
        prefs.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, null);
    }

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

    public boolean hasValidAccessToken() {
        String token = getAccessToken();

        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        Long expiration = getJwtExpiration(token);

        if (expiration == null) {
            return false;
        }

        long nowSeconds = System.currentTimeMillis() / 1000L;

        return expiration > nowSeconds;
    }

    private Long getJwtExpiration(String jwt) {
        try {
            String[] parts = jwt.split("\\.");

            if (parts.length < 2) {
                return null;
            }

            byte[] decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP);
            String payload = new String(decodedBytes, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(payload);

            if (!jsonObject.has("exp")) {
                return null;
            }

            return jsonObject.getLong("exp");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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