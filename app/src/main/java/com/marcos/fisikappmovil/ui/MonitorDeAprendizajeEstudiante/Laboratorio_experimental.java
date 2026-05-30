package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.PracticaExperimental;

/**
 * Activity que muestra la teoría y fórmulas del laboratorio experimental.
 * Permite al usuario iniciar la práctica real.
 */
public class Laboratorio_experimental extends AppCompatActivity {

    private Button btnEmpezarPractica;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_laboratorio_experimental);

        // Ajuste de paddings para barras del sistema
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Inicializar vistas
        btnEmpezarPractica = findViewById(R.id.btnEmpezarPractica);
        btnBack = findViewById(R.id.btnBack);

        // Configurar botón para regresar
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Configurar botón para ir a la Práctica Experimental
        if (btnEmpezarPractica != null) {
            btnEmpezarPractica.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Laboratorio_experimental.this, PracticaExperimental.class);
                    startActivity(intent);
                }
            });
        }
    }
}
