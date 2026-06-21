package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

public class PreguntasLaboratorioActivity extends AppCompatActivity {

    private int ordenPaso = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preguntas_laboratorio);

        ordenPaso = getIntent().getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);

        EditText etRespuesta1 = findViewById(R.id.etRespuesta1);
        EditText etRespuesta2 = findViewById(R.id.etRespuesta2);
        Button btnGuardar = findViewById(R.id.btnGuardarPreguntas);

        btnGuardar.setOnClickListener(v -> {
            if (etRespuesta1.getText().toString().trim().isEmpty()) {
                etRespuesta1.setError("Responde esta pregunta");
                return;
            }

            if (etRespuesta2.getText().toString().trim().isEmpty()) {
                etRespuesta2.setError("Responde esta pregunta");
                return;
            }

            completarPaso();
        });
    }

    private void completarPaso() {
        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }
}