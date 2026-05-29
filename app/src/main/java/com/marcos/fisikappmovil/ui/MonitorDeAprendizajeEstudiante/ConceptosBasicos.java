package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

public class ConceptosBasicos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conceptos_basicos);

        // =========================
        // BOTÓN REGRESAR
        // =========================
        /*
        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        */

        // =========================
        // BOTÓN SIGUIENTE ETAPA
        // =========================
        /*
        Button btnSiguiente = findViewById(R.id.btnSiguienteEtapa);

        if (btnSiguiente != null) {
            btnSiguiente.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ConceptosBasicos.this, PracticaConceptos.class);
                    startActivity(intent);
                }
            });
        }
        */

        // =========================
        // TABS
        // =========================
        TextView tabConceptos = findViewById(R.id.tabConceptos);
        TextView tabFormulas = findViewById(R.id.tabFormulas);


        // =========================
        // LAYOUTS
        // =========================
        LinearLayout layoutConceptos = findViewById(R.id.layoutConceptos);
        LinearLayout layoutFormulas = findViewById(R.id.layoutFormulas);


        // =========================
        // CLICK CONCEPTOS
        // =========================
        tabConceptos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layoutConceptos.setVisibility(View.VISIBLE);
                layoutFormulas.setVisibility(View.GONE);


                tabConceptos.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                tabFormulas.setTextColor(getResources().getColor(android.R.color.darker_gray));

            }
        });

        // =========================
        // CLICK FORMULAS
        // =========================
        tabFormulas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layoutConceptos.setVisibility(View.GONE);
                layoutFormulas.setVisibility(View.VISIBLE);


                tabConceptos.setTextColor(getResources().getColor(android.R.color.darker_gray));
                tabFormulas.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

            }
        });

        // =========================
        // CLICK BIBLIOGRAFIA
        // =========================

        });

    }
}