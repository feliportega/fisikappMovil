package com.marcos.fisikappmovil;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class InformeLaboratorio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_laboratorio);

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

        // Botón finalizar (simulación de guardado)
        View btnFinalizar = findViewById(R.id.btnFinalizar);
        if (btnFinalizar != null) {
            btnFinalizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(InformeLaboratorio.this, "Informe guardado correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // Regresa al menú principal
                }
            });
        }
    }
}
