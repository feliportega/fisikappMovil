package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

public class LecturaLaboratorioActivity extends AppCompatActivity {

    private int ordenPaso = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lectura_laboratorio);

        ordenPaso = getIntent().getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);

        TextView tvTitulo = findViewById(R.id.tvTituloLecturaLab);
        TextView tvContenido = findViewById(R.id.tvContenidoLecturaLab);
        Button btnCompletar = findViewById(R.id.btnCompletarLecturaLab);

        tvTitulo.setText("Conceptos y fórmulas");
        tvContenido.setText(
                "Movimiento parabólico\n\n" +
                        "El movimiento parabólico combina un movimiento horizontal uniforme " +
                        "con un movimiento vertical acelerado por la gravedad.\n\n" +
                        "Fórmulas principales:\n\n" +
                        "x(t) = x₀ + v₀ cos(θ)t\n\n" +
                        "y(t) = y₀ + v₀ sin(θ)t - 1/2 gt²\n\n" +
                        "El estudiante debe comprender cómo el ángulo y la velocidad inicial " +
                        "afectan la trayectoria, el alcance y la altura máxima."
        );

        btnCompletar.setOnClickListener(v -> completarPaso());
    }

    private void completarPaso() {
        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }
}