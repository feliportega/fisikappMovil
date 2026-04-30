package com.marcos.fisikappmovil.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FaceVault {

    private static final String FILE_NAME = "face_vault";
    private static final String KEY_CONSENT = "face_consent_accepted";
    private static final String KEY_EMBEDDING = "face_embedding";

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
        }
    }

    public static boolean hasConsent(Context context) {
        try {
            return getPrefs(context).getBoolean(KEY_CONSENT, false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void saveEmbedding(Context context, float[] embedding) {
        try {
            if (embedding == null || embedding.length == 0) return;

            ByteBuffer buffer = ByteBuffer.allocate(embedding.length * 4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            for (float v : embedding) {
                buffer.putFloat(v);
            }

            String base64 = Base64.encodeToString(buffer.array(), Base64.NO_WRAP);

            getPrefs(context).edit().putString(KEY_EMBEDDING, base64).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float[] getEmbedding(Context context) {
        try {
            String base64 = getPrefs(context).getString(KEY_EMBEDDING, null);
            if (base64 == null || base64.isEmpty()) return null;

            byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);
            ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

            int len = bytes.length / 4;
            float[] embedding = new float[len];

            for (int i = 0; i < len; i++) {
                embedding[i] = buffer.getFloat();
            }

            return embedding;

        } catch (Exception e) {
            e.printStackTrace();
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
        }
    }

    public static void clearConsent(Context context) {
        try {
            getPrefs(context).edit().remove(KEY_CONSENT).apply();
        } catch (Exception e) {
            e.printStackTrace();
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
        }
    }
}