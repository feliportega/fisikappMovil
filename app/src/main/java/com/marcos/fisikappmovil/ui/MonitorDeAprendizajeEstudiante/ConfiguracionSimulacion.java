package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

public class ConfiguracionSimulacion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pasos_del_laboratorio);

        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        Button btnSiguiente = findViewById(R.id.btnSiguienteEtapa);

        if (btnSiguiente != null) {
            btnSiguiente.setOnClickListener(v -> {

                // Navegar a la pantalla SimulacionAR
                Intent intent = new Intent(
                        ConfiguracionSimulacion.this,
                        SimulacionAR.class
                );

                startActivity(intent);
            });
        }
    }
}