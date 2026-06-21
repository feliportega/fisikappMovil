package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;

public class EnvioEntregaLaboratorioActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvEstadoEnvio;
    private TextView tvResumenEntrega;
    private TextView tvCalificacionEstado;
    private Button btnEnviarEntrega;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio_entrega_laboratorio);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        initListeners();
        pintarResumen();
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackEnvioEntrega);
        tvEstadoEnvio = findViewById(R.id.tvEstadoEnvio);
        tvResumenEntrega = findViewById(R.id.tvResumenEntrega);
        tvCalificacionEstado = findViewById(R.id.tvCalificacionEstado);
        btnEnviarEntrega = findViewById(R.id.btnEnviarEntrega);
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEnviarEntrega.setOnClickListener(v -> confirmarEnvio());
    }

    private void pintarResumen() {
        boolean enviada = sessionStore.isEntregaEnviada(asignacionId);

        if (enviada) {
            tvEstadoEnvio.setText("Entrega enviada");
            tvCalificacionEstado.setText("Calificación: pendiente de revisión");
            btnEnviarEntrega.setText("Finalizar");
        } else {
            tvEstadoEnvio.setText("Lista para enviar");
            tvCalificacionEstado.setText("Calificación: pendiente");
            btnEnviarEntrega.setText("Enviar entrega");
        }

        tvResumenEntrega.setText(
                "Resumen de entrega\n\n" +
                        "• Lectura y conceptos: completados\n" +
                        "• Preguntas de comprensión: completadas\n" +
                        "• Práctica experimental: completada\n" +
                        "• Datos experimentales: registrados\n" +
                        "• Práctica simulada AR: completada\n" +
                        "• Comparación de resultados: completada\n" +
                        "• Informe y conclusiones: completados\n\n" +
                        "Al enviar, la información quedará pendiente de revisión por IA/instructor."
        );
    }

    private void confirmarEnvio() {
        if (sessionStore.isEntregaEnviada(asignacionId)) {
            completarPaso();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Enviar entrega")
                .setMessage("¿Deseas enviar tu laboratorio? Después de enviarlo quedará pendiente de calificación.")
                .setPositiveButton("Enviar", (dialog, which) -> simularEnvio())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void simularEnvio() {
        setLoading(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            sessionStore.marcarEntregaEnviada(asignacionId);

            setLoading(false);
            pintarResumen();

            Toast.makeText(
                    this,
                    "Entrega enviada. Calificación pendiente.",
                    Toast.LENGTH_LONG
            ).show();

            completarPaso();

        }, 900);
    }

    private void setLoading(boolean loading) {
        btnEnviarEntrega.setEnabled(!loading);

        if (loading) {
            btnEnviarEntrega.setText("Enviando...");
        } else {
            btnEnviarEntrega.setText(
                    sessionStore.isEntregaEnviada(asignacionId)
                            ? "Finalizar"
                            : "Enviar entrega"
            );
        }
    }

    private void completarPaso() {
        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }
}