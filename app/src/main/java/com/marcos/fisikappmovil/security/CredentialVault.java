package com.marcos.fisikappmovil.security;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class CredentialVault {

    private static final String FILE_NAME = "credential_vault";

    private static final String KEY_CORREO = "correo";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_CREDENTIALS_ENABLED = "credentials_enabled";

    private static SharedPreferences getPrefs(Context context) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public static void saveCredentials(Context context, String correo, String password) {
        try {
            if (correo == null || correo.trim().isEmpty()) {
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                return;
            }

            getPrefs(context).edit()
                    .putString(KEY_CORREO, correo.trim())
                    .putString(KEY_PASSWORD, password)
                    .putBoolean(KEY_CREDENTIALS_ENABLED, true)
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCorreo(Context context) {
        try {
            return getPrefs(context).getString(KEY_CORREO, null);
        } catch (Exception e) {
            e.printStackTrace();

            if (SecurePrefsRecovery.isCryptoFailure(e)) {
                SecurePrefsRecovery.deleteEncryptedPrefs(context, FILE_NAME);
            }

            return null;
        }
    }

    public static String getPassword(Context context) {
        try {
            return getPrefs(context).getString(KEY_PASSWORD, null);
        } catch (Exception e) {
            e.printStackTrace();

            if (SecurePrefsRecovery.isCryptoFailure(e)) {
                SecurePrefsRecovery.deleteEncryptedPrefs(context, FILE_NAME);
            }

            return null;
        }
    }

    public static boolean hasCredentials(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);

            boolean enabled = prefs.getBoolean(KEY_CREDENTIALS_ENABLED, false);
            String correo = prefs.getString(KEY_CORREO, null);
            String password = prefs.getString(KEY_PASSWORD, null);

            return enabled
                    && correo != null
                    && !correo.trim().isEmpty()
                    && password != null
                    && !password.trim().isEmpty();

        } catch (Exception e) {
            e.printStackTrace();

            if (SecurePrefsRecovery.isCryptoFailure(e)) {
                SecurePrefsRecovery.deleteEncryptedPrefs(context, FILE_NAME);
            }

            return false;
        }
    }

    public static void clearCredentials(Context context) {
        try {
            getPrefs(context).edit()
                    .remove(KEY_CORREO)
                    .remove(KEY_PASSWORD)
                    .remove(KEY_CREDENTIALS_ENABLED)
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}