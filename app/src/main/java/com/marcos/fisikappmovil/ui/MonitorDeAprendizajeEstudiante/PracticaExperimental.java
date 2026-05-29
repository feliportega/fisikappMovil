package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

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

public class PracticaExperimental extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnContinuar;
    
    // Elementos del acordeón
    private LinearLayout layoutPaso1, layoutPaso2;
    private ImageView imgPaso1, imgPaso2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practica_experimental);

        // Inicializar vistas básicas
        btnBack = findViewById(R.id.btnBack);
        btnContinuar = findViewById(R.id.btnContinuar);

        // Inicializar elementos del acordeón
        layoutPaso1 = findViewById(R.id.layoutPaso1);
        layoutPaso2 = findViewById(R.id.layoutPaso2);
        imgPaso1 = findViewById(R.id.imgPaso1);
        imgPaso2 = findViewById(R.id.imgPaso2);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Configurar clics para el efecto acordeón
        layoutPaso1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleStep(imgPaso1, (ViewGroup) layoutPaso1.getParent());
            }
        });

        layoutPaso2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleStep(imgPaso2, (ViewGroup) layoutPaso2.getParent());
            }
        });

        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegación futura
            }
        });
    }

    /**
     * Alterna la visibilidad de la imagen con una animación suave
     */
    private void toggleStep(ImageView imageView, ViewGroup parent) {
        // Animación de transición suave
        TransitionManager.beginDelayedTransition(parent);
        
        if (imageView.getVisibility() == View.GONE) {
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }
}
