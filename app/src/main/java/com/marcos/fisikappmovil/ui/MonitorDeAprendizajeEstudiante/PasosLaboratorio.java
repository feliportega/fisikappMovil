package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

/**
 * Activity que actúa como el "Mapa de Ruta" o Roadmap del laboratorio.
 * Permite al usuario visualizar su progreso e iniciar las diferentes etapas.
 */
public class PasosLaboratorio extends AppCompatActivity {

    private LinearLayout btnIniciarCamino;
    private int idLaboratorio = -1; // Variable para guardar el ID dinámico

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasos_laboratorio);

        // ==========================================
        // 1. RECUPERAR EL ID DEL LABORATORIO SELECCIONADO
        // ==========================================
        idLaboratorio = getIntent().getIntExtra("LABORATORIO_ID", -1);
        Log.d("PASOS_LAB", "Abriendo la ruta del Laboratorio ID: " + idLaboratorio);

        // Inicializar el contenedor del Paso 1
        btnIniciarCamino = findViewById(R.id.btnIniciarCamino);

        // ==========================================
        // 2. CONFIGURACIÓN DEL FLUJO (Paso 1 -> Simulación AR)
        // ==========================================
        if (btnIniciarCamino != null) {
            btnIniciarCamino.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PasosLaboratorio.this, SimulacionAR.class);

                    // IMPORTANTE: Le pasamos el ID a la simulación para que la escena de Realidad Aumentada
                    // sepa qué objetos 3D o qué experimentos cargar.
                    intent.putExtra("LABORATORIO_ID", idLaboratorio);

                    startActivity(intent);
                }
            });
        }

        // Nota futura: Si deseas que los textos de los pasos cambien según el ID,
        // aquí puedes llamar a un método como 'cargarPasosDesdeBackend(idLaboratorio);'
    }
}