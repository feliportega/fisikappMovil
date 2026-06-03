package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;


public class PasosLaboratorio extends AppCompatActivity {

    private LinearLayout btnConceptos;
    private int idLaboratorio = -1; // Variable para guardar el ID dinámico

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasos_laboratorio);

        // 1. RECUPERAR EL ID DEL LABORATORIO SELECCIONADO
        idLaboratorio = getIntent().getIntExtra("LABORATORIO_ID", -1);
        Log.d("PASOS_LAB", "Abriendo la ruta del Laboratorio ID: " + idLaboratorio);

        // Boton Conceptos
        btnConceptos = findViewById(R.id.btnConceptos);

        if (btnConceptos != null) {
            btnConceptos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PasosLaboratorio.this, ConceptosBasicos.class);
                    intent.putExtra("LABORATORIO_ID", idLaboratorio);

                    startActivity(intent);
                }
            });
        }
    }
}