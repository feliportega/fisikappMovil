package com.marcos.fisikappmovil.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ImagePickerHelper {

    // Interfaz para comunicar la selección del usuario a la Activity
    public interface ImagePickerListener {
        void onGallerySelected();
        void onCameraSelected();
    }

    public static final int CAMERA_PERMISSION_CODE = 100;
    private final Context context;
    private final ImagePickerListener listener;

    public ImagePickerHelper(Context context, ImagePickerListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void showImagePickerDialog() {
        CharSequence[] options = {"Tomar foto", "Elegir de galería", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Cambiar foto de perfil");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Tomar foto")) {
                    checkCameraPermissionAndOpen();
                } else if (options[item].equals("Elegir de galería")) {
                    listener.onGallerySelected();
                } else if (options[item].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            listener.onCameraSelected();
        } else {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }
}