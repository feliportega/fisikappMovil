package com.marcos.fisikappmovil.security;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class SecurePrefsRecovery {

    private static final String TAG = "SecurePrefsRecovery";

    public static void deleteEncryptedPrefs(Context context, String fileName) {
        try {
            if (context == null || fileName == null || fileName.trim().isEmpty()) {
                return;
            }

            // SharedPreferences normalmente vive como:
            // /data/data/<package>/shared_prefs/<fileName>.xml
            File sharedPrefsDir = new File(context.getApplicationInfo().dataDir, "shared_prefs");

            File prefsFile = new File(sharedPrefsDir, fileName + ".xml");
            File backupFile = new File(sharedPrefsDir, fileName + ".xml.bak");

            boolean prefsDeleted = !prefsFile.exists() || prefsFile.delete();
            boolean backupDeleted = !backupFile.exists() || backupFile.delete();

            Log.w(TAG, "Vault corrupto eliminado: " + fileName
                    + " prefsDeleted=" + prefsDeleted
                    + " backupDeleted=" + backupDeleted);

        } catch (Exception e) {
            Log.e(TAG, "No se pudo eliminar vault corrupto: " + fileName, e);
        }
    }

    public static boolean isCryptoFailure(Exception e) {
        if (e == null) {
            return false;
        }

        Throwable current = e;

        while (current != null) {
            String name = current.getClass().getName();

            if (name.contains("AEADBadTagException")
                    || name.contains("KeyPermanentlyInvalidatedException")
                    || name.contains("InvalidKeyException")
                    || name.contains("BadPaddingException")
                    || name.contains("IllegalBlockSizeException")
                    || name.contains("GeneralSecurityException")
                    || name.contains("KeyStoreException")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}