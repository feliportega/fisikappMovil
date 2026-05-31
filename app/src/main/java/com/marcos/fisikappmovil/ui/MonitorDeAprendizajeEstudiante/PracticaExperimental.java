package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.marcos.fisikappmovil.R;

/**
 * Activity que guía al usuario a través del experimento físico.
 * Muestra materiales y un procedimiento paso a paso en formato acordeón.
 */
public class PracticaExperimental extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnContinuar;
    
    // Elementos del acordeón
    private LinearLayout layoutPaso1, layoutPaso2, layoutPaso3, layoutPaso4, layoutPaso5, layoutPaso6, layoutPaso7;
    private ImageView imgPaso1, imgPaso2, imgPaso3, imgPaso4, imgPaso5, imgPaso6, imgPaso7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practica_experimental);

        // Inicializar vistas básicas
        btnBack = findViewById(R.id.btnBack);
        btnContinuar = findViewById(R.id.btnContinuar);

        // Inicializar elementos del acordeón
        initAccordionViews();

        // Botón atrás
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Configurar clics para expandir/colapsar pasos
        setupStepListeners();

        // BOTÓN FINAL (Punto 7 del flujo): Regresa al Roadmap (Pasos del laboratorio)
        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PracticaExperimental.this, PasosLaboratorio.class);
                    // Usamos estas flags para no crear muchas pantallas repetidas en el historial
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private void initAccordionViews() {
        layoutPaso1 = findViewById(R.id.layoutPaso1);
        layoutPaso2 = findViewById(R.id.layoutPaso2);
        layoutPaso3 = findViewById(R.id.layoutPaso3);
        layoutPaso4 = findViewById(R.id.layoutPaso4);
        layoutPaso5 = findViewById(R.id.layoutPaso5);
        layoutPaso6 = findViewById(R.id.layoutPaso6);
        layoutPaso7 = findViewById(R.id.layoutPaso7);

        imgPaso1 = findViewById(R.id.imgPaso1);
        imgPaso2 = findViewById(R.id.imgPaso2);
        imgPaso3 = findViewById(R.id.imgPaso3);
        imgPaso4 = findViewById(R.id.imgPaso4);
        imgPaso5 = findViewById(R.id.imgPaso5);
        imgPaso6 = findViewById(R.id.imgPaso6);
        imgPaso7 = findViewById(R.id.imgPaso7);
    }

    private void setupStepListeners() {
        if (layoutPaso1 != null) layoutPaso1.setOnClickListener(v -> toggleStep(imgPaso1, (ViewGroup) layoutPaso1.getParent()));
        if (layoutPaso2 != null) layoutPaso2.setOnClickListener(v -> toggleStep(imgPaso2, (ViewGroup) layoutPaso2.getParent()));
        if (layoutPaso3 != null) layoutPaso3.setOnClickListener(v -> toggleStep(imgPaso3, (ViewGroup) layoutPaso3.getParent()));
        if (layoutPaso4 != null) layoutPaso4.setOnClickListener(v -> toggleStep(imgPaso4, (ViewGroup) layoutPaso4.getParent()));
        if (layoutPaso5 != null) layoutPaso5.setOnClickListener(v -> toggleStep(imgPaso5, (ViewGroup) layoutPaso5.getParent()));
        if (layoutPaso6 != null) layoutPaso6.setOnClickListener(v -> toggleStep(imgPaso6, (ViewGroup) layoutPaso6.getParent()));
        if (layoutPaso7 != null) layoutPaso7.setOnClickListener(v -> toggleStep(imgPaso7, (ViewGroup) layoutPaso7.getParent()));
    }

    private void toggleStep(ImageView imageView, ViewGroup parent) {
        if (imageView == null) return;
        TransitionManager.beginDelayedTransition(parent);
        if (imageView.getVisibility() == View.GONE) {
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }
}
