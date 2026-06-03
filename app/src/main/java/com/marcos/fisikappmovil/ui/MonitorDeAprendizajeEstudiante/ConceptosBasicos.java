package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.models.MenuActivity;


public class ConceptosBasicos extends MenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conceptos_basicos);
        configurarMenu();

        // Botón para regresar
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // CONFIGURACIÓN DEL FLUJO (Punto 4 al 5): De Teoría a Fórmulas
        TextView btnVerFormulas = findViewById(R.id.btnFormulas);
        if (btnVerFormulas != null) {
            btnVerFormulas.setOnClickListener(v -> {
                Intent intent = new Intent(ConceptosBasicos.this, Laboratorio_experimental.class);
                startActivity(intent);
            });
        }
    }
}
