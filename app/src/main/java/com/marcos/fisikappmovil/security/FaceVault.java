package com.marcos.fisikappmovil.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FaceVault {

    private static final String FILE_NAME = "face_vault";
    private static final String KEY_CONSENT = "face_consent_accepted";
    private static final String KEY_EMBEDDING = "face_embedding";

    //Borrelo mijo
    private static final String TAG = "FaceVault";

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

    public static void saveConsent(Context context, boolean accepted) {
        try {
            getPrefs(context).edit().putBoolean(KEY_CONSENT, accepted).apply();
        } catch (Exception e) {
            e.printStackTrace();

            if (SecurePrefsRecovery.isCryptoFailure(e)) {
                SecurePrefsRecovery.deleteEncryptedPrefs(context, FILE_NAME);
            }
        }
    }

    public static boolean hasConsent(Context context) {
        try {
            return getPrefs(context).getBoolean(KEY_CONSENT, false);
        } catch (Exception e) {
            e.printStackTrace();

            if (SecurePrefsRecovery.isCryptoFailure(e)) {
                SecurePrefsRecovery.deleteEncryptedPrefs(context, FILE_NAME);
            }

            return false;
        }
    }

    public static void saveEmbedding(Context context, float[] embedding) {
        try {
            if (embedding == null || embedding.length == 0){
                Log.e(TAG, "saveEmbedding: embedding null o vacío");
                return;
            }

            ByteBuffer buffer = ByteBuffer.allocate(embedding.length * 4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            for (float v : embedding) {
                buffer.putFloat(v);
            }

            String base64 = Base64.encodeToString(buffer.array(), Base64.NO_WRAP);

            getPrefs(context).edit().putString(KEY_EMBEDDING, base64).apply();

            //Eliminar tambien
            float[] check = getEmbedding(context);
            Log.d(TAG, "saveEmbedding OK. len=" + embedding.length +
                    " base64Len=" + base64.length() +
                    " checkLen=" + (check != null ? check.length : -1));

        } catch (Exception e) {
            Log.e(TAG, "saveEmbedding ERROR", e);
            e.printStackTrace();
            if (SecurePrefsRecovery.isCryptoFailure(e)) {
                SecurePrefsRecovery.deleteEncryptedPrefs(context, FILE_NAME);
            }
        }
    }

    public static float[] getEmbedding(Context context) {
        try {
            String base64 = getPrefs(context).getString(KEY_EMBEDDING, null);

            Log.d(TAG, "getEmbedding base64 null? " + (base64 == null) +
                    " len=" + (base64 != null ? base64.length() : -1));

            if (base64 == null || base64.isEmpty()) return null;

            byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);
            ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

            int len = bytes.length / 4;
            float[] embedding = new float[len];

            for (int i = 0; i < len; i++) {
                embedding[i] = buffer.getFloat();
            }

            Log.d(TAG, "getEmbedding OK len=" + embedding.length);
            return embedding;

        } catch (Exception e) {
            Log.e(TAG, "getEmbedding ERROR", e);
            e.printStackTrace();

            if (SecurePrefsRecovery.isCryptoFailure(e)) {
                SecurePrefsRecovery.deleteEncryptedPrefs(context, FILE_NAME);
            }

            return null;
        }
    }

    public static boolean hasEmbedding(Context context) {
        return getEmbedding(context) != null;
    }

    public static void clearEmbedding(Context context) {
        try {
            getPrefs(context).edit().remove(KEY_EMBEDDING).apply();
        } catch (Exception e) {
            e.printStackTrace();

            if (SecurePrefsRecovery.isCryptoFailure(e)) {
                SecurePrefsRecovery.deleteEncryptedPrefs(context, FILE_NAME);
            }
        }
    }

    public static void clearConsent(Context context) {
        try {
            getPrefs(context).edit().remove(KEY_CONSENT).apply();
        } catch (Exception e) {
            e.printStackTrace();

            if (SecurePrefsRecovery.isCryptoFailure(e)) {
                SecurePrefsRecovery.deleteEncryptedPrefs(context, FILE_NAME);
            }
        }
    }

    public static void clearAll(Context context) {
        try {
            getPrefs(context).edit()
                    .remove(KEY_EMBEDDING)
                    .remove(KEY_CONSENT)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();

            if (SecurePrefsRecovery.isCryptoFailure(e)) {
                SecurePrefsRecovery.deleteEncryptedPrefs(context, FILE_NAME);
            }
        }
    }
}