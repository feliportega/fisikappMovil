package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import static java.security.AccessController.getContext;

import android.content.DialogInterface;
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
import com.marcos.fisikappmovil.ui.common.StepCompletionOverlay;

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

        btnEnviarEntrega.setOnClickListener(v -> {
            if (laboratorioYaEntregado()) {
                finish();
                return;
            }
            confirmarEnvio();
        });
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

        boolean tienePractica = existeJson(sessionStore.getPracticaExperimentalJson(asignacionId));
        boolean tieneEvidencias = existeJson(sessionStore.getEvidenciasJson(asignacionId));
        boolean tieneAr = existeJson(sessionStore.getUnityResultJson(asignacionId));
        boolean tieneComparacion = existeJson(sessionStore.getComparacionResultadosJson(asignacionId));
        boolean tieneInforme = existeJson(sessionStore.getInformeLaboratorioJson(asignacionId));

        tvResumenEntrega.setText(
                "Resumen de entrega\n\n" +
                        "• Práctica experimental: " + estadoTexto(tienePractica) + "\n" +
                        "• Evidencias: " + estadoTexto(tieneEvidencias) + "\n" +
                        "• Práctica simulada AR: " + estadoTexto(tieneAr) + "\n" +
                        "• Comparación de resultados: " + estadoTexto(tieneComparacion) + "\n" +
                        "• Informe de laboratorio: " + estadoTexto(tieneInforme) + "\n\n" +
                        "Al enviar, la información quedará pendiente de sincronización con el backend final."
        );
    }

    private boolean existeJson(String json) {
        return json != null && !json.trim().isEmpty();
    }

    private String estadoTexto(boolean ok) {
        return ok ? "registrada" : "pendiente";
    }
    private void confirmarEnvio() {
        if (sessionStore.isEntregaEnviada(asignacionId)) {
            Intent data = new Intent();
            data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
            setResult(RESULT_OK, data);
            finish();
            return;
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(EnvioEntregaLaboratorioActivity.this)
                .setTitle("Enviar entrega")
                .setMessage("La entrega quedará guardada localmente. La sincronización final dependerá del backend.")
                .setPositiveButton("Guardar entrega", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void simularEnvio() {
        setLoading(true);

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            setLoading(false);

            sessionStore.marcarEntregaEnviada(asignacionId);

            if (asignacionId > 0 && ordenPaso > 0) {
                sessionStore.completarPasoYDesbloquearSiguiente(asignacionId, ordenPaso);
            }

            /*
            Toast.makeText(
                    this,
                    "Entrega guardada localmente. Sincronización final pendiente de backend.",
                    Toast.LENGTH_LONG
            ).show();*/

            pintarResumen();

            Intent data = new Intent();
            data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);

            /*
            StepCompletionOverlay.show(this, () -> {
                setResult(RESULT_OK, data);
                finish();
            });*/
            StepCompletionOverlay.show(this, () -> {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    setResult(RESULT_OK, data);
                    finish();
                }, 900L);
            });

        }, 1200);
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

    // Helper de estado
    private boolean laboratorioYaEntregado() {
        return asignacionId > 0
                && sessionStore.isEntregaEnviada(asignacionId);
    }

}