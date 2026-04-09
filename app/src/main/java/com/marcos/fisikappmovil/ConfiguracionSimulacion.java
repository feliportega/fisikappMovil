package com.marcos.fisikappmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ConfiguracionSimulacion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_simulacion);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        Button btnSiguiente = findViewById(R.id.btnSiguienteEtapa);
        if (btnSiguiente != null) {
            btnSiguiente.setOnClickListener(v -> {
                // Aquí iría la pantalla de Realidad Aumentada
                // Intent intent = new Intent(this, SimulacionAR.class);
                // startActivity(intent);
            });
        }
    }
}
