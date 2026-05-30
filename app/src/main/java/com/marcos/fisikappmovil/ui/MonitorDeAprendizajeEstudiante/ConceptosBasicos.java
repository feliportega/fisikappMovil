package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.Laboratorio_experimental;

/**
 * Activity que muestra los conceptos teóricos del laboratorio.
 */
public class ConceptosBasicos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conceptos_basicos);

        // Botón para regresar
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // CONFIGURACIÓN DEL FLUJO (Punto 4 al 5): De Teoría a Fórmulas
        Button btnVerFormulas = findViewById(R.id.btnVerFormulas);
        if (btnVerFormulas != null) {
            btnVerFormulas.setOnClickListener(v -> {
                Intent intent = new Intent(ConceptosBasicos.this, Laboratorio_experimental.class);
                startActivity(intent);
            });
        }
    }
}
