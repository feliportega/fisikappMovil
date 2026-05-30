package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasos_laboratorio);

        // Inicializar el contenedor del Paso 1
        btnIniciarCamino = findViewById(R.id.btnIniciarCamino);

        // CONFIGURACIÓN DEL FLUJO (Punto 2 al 3): Al tocar el paso 1 va a Simulación AR
        if (btnIniciarCamino != null) {
            btnIniciarCamino.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PasosLaboratorio.this, SimulacionAR.class);
                    startActivity(intent);
                }
            });
        }
    }
}
