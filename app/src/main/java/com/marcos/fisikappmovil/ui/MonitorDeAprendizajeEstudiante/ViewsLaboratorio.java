package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.marcos.fisikappmovil.R;

public class ViewsLaboratorio extends AppCompatActivity {

    CardView btninforme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views_laboratorio);

        btninforme = findViewById(R.id.btnInforme);
        btninforme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent lyfinal = new Intent(ViewsLaboratorio.this, InformeLaboratorio.class);
                startActivity(lyfinal);
            }
        });


        // 1. Botón Conceptos Básicos
        CardView btnConceptos = findViewById(R.id.btnConceptos);
        if (btnConceptos != null) {
            btnConceptos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewsLaboratorio.this, ConceptosBasicos.class);
                    startActivity(intent);
                }
            });
        }

        // 2. Botón Práctica de Conceptos
        CardView btnPractica = findViewById(R.id.btnPractica);
        if (btnPractica != null) {
            btnPractica.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewsLaboratorio.this, PracticaConceptos.class);
                    startActivity(intent);
                }
            });
        }
    }
}
