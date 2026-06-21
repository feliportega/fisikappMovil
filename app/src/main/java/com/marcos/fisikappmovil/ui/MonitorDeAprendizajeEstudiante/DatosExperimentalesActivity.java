package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

public class DatosExperimentalesActivity extends AppCompatActivity {

    private int ordenPaso = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paso_simple_laboratorio);

        ordenPaso = getIntent().getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);

        TextView tvTitulo = findViewById(R.id.tvTituloPasoSimple);
        TextView tvDescripcion = findViewById(R.id.tvDescripcionPasoSimple);
        Button btnCompletar = findViewById(R.id.btnCompletarPasoSimple);

        tvTitulo.setText("Datos experimentales");
        tvDescripcion.setText("Aquí se registrarán las mediciones de la práctica experimental.");

        btnCompletar.setOnClickListener(v -> completarPaso());
    }

    private void completarPaso() {
        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }
}