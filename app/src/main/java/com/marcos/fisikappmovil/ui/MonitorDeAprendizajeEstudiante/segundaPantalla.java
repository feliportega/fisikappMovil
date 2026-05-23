package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.marcos.fisikappmovil.R;

public class segundaPantalla extends AppCompatActivity {

    private LinearLayout containerImagenes;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_final_2);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnConclusiones = findViewById(R.id.btnConclusiones);
        CardView btnAgregarCapturas = findViewById(R.id.btnAgregarCapturas);
        containerImagenes = findViewById(R.id.containerImagenes);

        // Configurar el lanzador de la galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleGalleryResult(result.getData());
                    }
                }
        );

        btnBack.setOnClickListener(v -> finish());

        btnConclusiones.setOnClickListener(v -> {
            Intent intent = new Intent(segundaPantalla.this, tercerapantalla.class);
            startActivity(intent);
        });

        btnAgregarCapturas.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryLauncher.launch(Intent.createChooser(intent, "Selecciona capturas"));
    }

    private void handleGalleryResult(Intent data) {
        if (data.getClipData() != null) {
            // Múltiples imágenes seleccionadas
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri imageUri = clipData.getItemAt(i).getUri();
                addImageToContainer(imageUri);
            }
        } else if (data.getData() != null) {
            // Una sola imagen seleccionada
            Uri imageUri = data.getData();
            addImageToContainer(imageUri);
        }
    }

    private void addImageToContainer(Uri uri) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                300, // ancho en px
                300  // alto en px
        );
        params.setMargins(0, 0, 16, 0);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(uri);
        
        // Agregar padding o background opcional
        imageView.setPadding(4, 4, 4, 4);
        
        containerImagenes.addView(imageView);
    }
}