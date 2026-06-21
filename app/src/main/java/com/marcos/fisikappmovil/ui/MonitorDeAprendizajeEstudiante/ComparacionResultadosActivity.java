package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

public class ComparacionResultadosActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvDistanciaSimulada;
    private TextView tvDistanciaExperimental;
    private TextView tvDiferenciaDistancia;
    private TextView tvResultadoAr;
    private EditText etAnalisisComparacion;
    private Button btnGuardarComparacion;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparacion_resultados);

        readExtras();
        initViews();
        initListeners();
        pintarDatosMock();
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackComparacion);

        tvDistanciaSimulada = findViewById(R.id.tvDistanciaSimulada);
        tvDistanciaExperimental = findViewById(R.id.tvDistanciaExperimental);
        tvDiferenciaDistancia = findViewById(R.id.tvDiferenciaDistancia);
        tvResultadoAr = findViewById(R.id.tvResultadoAr);

        etAnalisisComparacion = findViewById(R.id.etAnalisisComparacion);
        btnGuardarComparacion = findViewById(R.id.btnGuardarComparacion);
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnGuardarComparacion.setOnClickListener(v -> validarYCompletar());
    }

    private void pintarDatosMock() {
        // Mock temporal.
        // Luego estos datos deben venir desde LaboratorioSessionStore:
        // - resultado de Unity / AR
        // - datos experimentales registrados por el estudiante

        double distanciaSimulada = 1.42;
        double distanciaExperimental = 1.38;
        double diferencia = Math.abs(distanciaSimulada - distanciaExperimental);

        tvDistanciaSimulada.setText(distanciaSimulada + " m");
        tvDistanciaExperimental.setText(distanciaExperimental + " m");
        tvDiferenciaDistancia.setText(String.format("%.2f m", diferencia));

        tvResultadoAr.setText(
                "Práctica simulada AR: objetivo impactado.\n" +
                        "Intentos usados: 3/4.\n" +
                        "Distancia final al objetivo: 0.03 m."
        );
    }

    private void validarYCompletar() {
        String analisis = etAnalisisComparacion.getText().toString().trim();

        if (analisis.isEmpty()) {
            etAnalisisComparacion.setError("Escribe tu comparación");
            return;
        }

        if (analisis.length() < 20) {
            etAnalisisComparacion.setError("Escribe un análisis un poco más completo");
            return;
        }

        Toast.makeText(this, "Comparación guardada", Toast.LENGTH_SHORT).show();

        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }
}