package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.models.MenuActivity;


public class PasosLaboratorio extends MenuActivity {

    private LinearLayout lnlConceptos;
    private int idLaboratorio = -1; // Variable para guardar el ID dinámico

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasos_laboratorio);
        configurarMenu();

        // 1. RECUPERAR EL ID DEL LABORATORIO SELECCIONADO
        idLaboratorio = getIntent().getIntExtra("LABORATORIO_ID", -1);
        Log.d("PASOS_LAB", "Abriendo la ruta del Laboratorio ID: " + idLaboratorio);

        // Boton Conceptos
        lnlConceptos = findViewById(R.id.lnlConceptos);

        if (lnlConceptos != null) {
            lnlConceptos.setOnClickListener(new View.OnClickListener() {
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