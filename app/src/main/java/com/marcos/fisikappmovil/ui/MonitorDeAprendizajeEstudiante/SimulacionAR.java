package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.Laboratorio_experimental;

/**
 * Activity que gestiona la Simulación de Realidad Aumentada.
 */
public class SimulacionAR extends AppCompatActivity {

    private Button btnIrAConceptos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulacion_ar);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // CONFIGURACIÓN DEL FLUJO (Punto 3 al 4): De Simulación a Conceptos Básicos
        btnIrAConceptos = findViewById(R.id.btnIrAConceptos);
        if (btnIrAConceptos != null) {
            btnIrAConceptos.setOnClickListener(v -> {
                Intent intent = new Intent(SimulacionAR.this, ConceptosBasicos.class);
                startActivity(intent);
            });
        }
    }
}
