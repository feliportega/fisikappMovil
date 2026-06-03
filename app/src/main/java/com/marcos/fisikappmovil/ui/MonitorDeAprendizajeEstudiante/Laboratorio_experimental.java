package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.models.MenuActivity;

public class Laboratorio_experimental extends MenuActivity {

    private Button btnEmpezarPractica;
    private ImageView btnBack;
    private Button btnConceptos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_laboratorio_experimental);
        configurarMenu();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        btnConceptos = findViewById(R.id.btnConceptos);
        btnEmpezarPractica = findViewById(R.id.btnEmpezarPractica);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnConceptos.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Laboratorio_experimental.this, ConceptosBasicos.class
            );
            startActivity(intent);
        });

        btnEmpezarPractica.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Laboratorio_experimental.this,
                    PracticaExperimental.class
            );
            startActivity(intent);
        });

    }
}