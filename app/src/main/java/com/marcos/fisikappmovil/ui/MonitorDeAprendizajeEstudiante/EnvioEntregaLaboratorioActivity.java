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
import com.marcos.fisikappmovil.ui.Laboratorio.GrupoLaboratoriosActivity;
import com.marcos.fisikappmovil.ui.common.LoadingOverlayView;

public class EnvioEntregaLaboratorioActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvEstadoEnvio;
    private TextView tvResumenEntrega;
    private TextView tvCalificacionEstado;
    private Button btnEnviarEntrega;
    private String grupoNombre;
    private String grupoCurso;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    private LoadingOverlayView loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio_entrega_laboratorio);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        initListeners();
        configurarBackPress();

        pintarResumen();
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);

        grupoNombre = intent.getStringExtra("GRUPO_NOMBRE");
        grupoCurso = intent.getStringExtra("GRUPO_CURSO");

        if (grupoNombre == null || grupoNombre.trim().isEmpty()) {
            grupoNombre = "Grupo académico";
        }

        if (grupoCurso == null || grupoCurso.trim().isEmpty()) {
            grupoCurso = "Física";
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackEnvioEntrega);
        tvEstadoEnvio = findViewById(R.id.tvEstadoEnvio);
        tvResumenEntrega = findViewById(R.id.tvResumenEntrega);
        tvCalificacionEstado = findViewById(R.id.tvCalificacionEstado);
        btnEnviarEntrega = findViewById(R.id.btnEnviarEntrega);
        loadingOverlay = new LoadingOverlayView(findViewById(R.id.loadingOverlay));
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> {
            if (loadingOverlay != null && loadingOverlay.isShowing()) {
                Toast.makeText(
                        this,
                        "Espera a que termine el envío.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            finish();
        });

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
            irAGrupoLaboratorios();
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

            Toast.makeText(
                    this,
                    "Entrega enviada. Calificación pendiente.",
                    Toast.LENGTH_LONG
            ).show();

            irAGrupoLaboratorios();

        }, 900);
    }

    private void setLoading(boolean loading) {
        if (loading) {
            loadingOverlay.show(
                    "Enviando laboratorio",
                    "Estamos registrando tu entrega. No cierres la aplicación."
            );

            btnEnviarEntrega.setEnabled(false);
        } else {
            loadingOverlay.hide();
            btnEnviarEntrega.setEnabled(true);
        }
    }

    private void completarPaso() {
        Intent data = new Intent();

        if (ordenPaso != -1) {
            data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        }

        setResult(RESULT_OK, data);
        finish();
    }

    private void irAGrupoLaboratorios() {
        Intent intent = new Intent(this, GrupoLaboratoriosActivity.class);

        intent.putExtra(GrupoLaboratoriosActivity.EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(GrupoLaboratoriosActivity.EXTRA_GRUPO_NOMBRE, grupoNombre);
        intent.putExtra(GrupoLaboratoriosActivity.EXTRA_GRUPO_CURSO, grupoCurso);

        /*
         * Limpia las pantallas del laboratorio ya finalizado:
         * EnvioEntrega → Pasos → Informe → etc.
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
    }

    private void configurarBackPress() {
        getOnBackPressedDispatcher().addCallback(
                this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (loadingOverlay != null && loadingOverlay.isShowing()) {
                            Toast.makeText(
                                    EnvioEntregaLaboratorioActivity.this,
                                    "Espera a que termine el envío.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        finish();
                    }
                }
        );
    }

}