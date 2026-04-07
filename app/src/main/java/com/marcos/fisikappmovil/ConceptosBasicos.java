package com.marcos.fisikappmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ConceptosBasicos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conceptos_basicos);

        // Botón para regresar
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Botón "Siguiente etapa" para ir a Práctica de Conceptos
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
    }
}
