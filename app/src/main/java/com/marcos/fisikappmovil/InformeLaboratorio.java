package com.marcos.fisikappmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InformeLaboratorio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_informe_laboratorio);

        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout layoutObservaciones = findViewById(R.id.layoutObservaciones);
        LinearLayout layoutConclusiones = findViewById(R.id.layoutConclusiones);
        Button btnEnviar = findViewById(R.id.btnEnviar);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Regresa a la pantalla anterior
            }
        });

        layoutObservaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InformeLaboratorio.this, segundaPantalla.class);
                startActivity(intent);
            }
        });

        layoutConclusiones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InformeLaboratorio.this, tercerapantalla.class);
                startActivity(intent);
            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InformeLaboratorio.this, cuartaPantalla.class);
                startActivity(intent);
            }
        });
    }
}