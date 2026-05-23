package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

public class cuartaPantalla extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_final_4);

        Button btnDescargar = findViewById(R.id.btnDescargar);

        btnDescargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambia esta URL por la URL real de tu servidor
                String url = "http://10.0.2.2:8000/api/descargar_informe/"; 
                iniciarDescarga(url);
            }
        });
    }

    private void iniciarDescarga(String url) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            
            // Configuración de la notificación de descarga
            request.setTitle("Informe Fisikapp.pdf");
            request.setDescription("Descargando el informe final del laboratorio...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            
            // Guardar en la carpeta pública de descargas del dispositivo
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Informe_Laboratorio_Fisikapp.pdf");
            
            // Obtener el servicio y encolar la descarga
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(this, "La descarga ha comenzado", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al descargar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}