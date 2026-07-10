package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.marcos.fisikappmovil.data.repository.LaboratorioRepository;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.remote.response.SubmitLaboratorioResponse;

import androidx.appcompat.app.AppCompatActivity;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
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

    private LaboratorioRepository laboratorioRepository;
    private TokenManager tokenManager;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio_entrega_laboratorio);

        sessionStore = new LaboratorioSessionStore(this);
        laboratorioRepository = new LaboratorioRepository();
        tokenManager = new TokenManager(this);

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
        boolean tienePreguntas = existeJson(sessionStore.getPreguntasJson(asignacionId));
        boolean tieneInforme = existeJson(sessionStore.getInformeLaboratorioJson(asignacionId));

        tvResumenEntrega.setText(
                "Resumen de entrega\n\n" +
                        "• Práctica experimental: " + estadoTexto(tienePractica) + "\n" +
                        "• Evidencias: " + estadoTexto(tieneEvidencias) + "\n" +
                        "• Práctica simulada AR: " + estadoTexto(tieneAr) + "\n" +
                        "• Comparación de resultados: " + estadoTexto(tieneComparacion) + "\n" +
                        "• Preguntas: " + estadoTexto(tienePreguntas) + "\n" +
                        "• Informe de laboratorio: " + estadoTexto(tieneInforme) + "\n\n" +
                        "Al enviar, la información será registrada en el backend para revisión."
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
            Intent intent = new Intent(EnvioEntregaLaboratorioActivity.this, Dashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(EnvioEntregaLaboratorioActivity.this)
                .setTitle("Enviar entrega")
                .setMessage("La entrega será enviada al backend para revisión y calificación.")
                .setPositiveButton("Enviar entrega", (dialog, which) -> enviarEntregaBackend())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void enviarEntregaBackend() {
        if (asignacionId <= 0) {
            Toast.makeText(this, "No se recibió la asignación del laboratorio.", Toast.LENGTH_LONG).show();
            return;
        }

        String authHeader = tokenManager.getAuthorizationHeader();

        if (authHeader == null || authHeader.trim().isEmpty()) {
            Toast.makeText(this, "Sesión no válida. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        JsonObject payload = buildSubmitPayload();

        if (payload == null) {
            Toast.makeText(this, "No se pudo construir la entrega.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        android.util.Log.d("SUBMIT_DEBUG", "Iniciando envío real al backend");
        android.util.Log.d("SUBMIT_DEBUG", "assignmentId=" + asignacionId);
        android.util.Log.d("SUBMIT_DEBUG", "authHeader existe=" + (authHeader != null && !authHeader.trim().isEmpty()));
        android.util.Log.d("SUBMIT_DEBUG", "payload=" + payload.toString());

        laboratorioRepository.submitMobileAssignment(
                authHeader,
                asignacionId,
                payload,
                result -> runOnUiThread(() -> {
                    setLoading(false);

                    android.util.Log.d("SUBMIT_DEBUG", "Respuesta recibida. success=" + result.isSuccess());
                    android.util.Log.d("SUBMIT_DEBUG", "statusCode=" + result.getStatusCode());

                    if (!result.isSuccess()) {
                        Toast.makeText(
                                this,
                                result.getErrorMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    SubmitLaboratorioResponse response = result.getData();

                    sessionStore.marcarEntregaEnviada(asignacionId);

                    if (asignacionId > 0 && ordenPaso > 0) {
                        sessionStore.completarPasoYDesbloquearSiguiente(asignacionId, ordenPaso);
                    }

                    pintarResumen();

                    String message = "Entrega enviada correctamente.";

                    if (response != null
                            && response.getMessage() != null
                            && !response.getMessage().trim().isEmpty()) {
                        message = response.getMessage();
                    }

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                    Intent data = new Intent();
                    data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);

                    StepCompletionOverlay.show(this, () -> {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent(EnvioEntregaLaboratorioActivity.this, Dashboard.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }, 900L);
                    });
                })
        );
    }

    private JsonObject buildSubmitPayload() {
        try {
            JsonObject root = new JsonObject();

            root.add("practice", parseObjectOrEmpty(sessionStore.getPracticaExperimentalJson(asignacionId)));
            root.add("simulation", parseObjectOrEmpty(sessionStore.getUnityResultJson(asignacionId)));
            root.add("comparison", parseObjectOrEmpty(sessionStore.getComparacionResultadosJson(asignacionId)));
            root.add("questions", parseArrayOrEmpty(sessionStore.getPreguntasJson(asignacionId)));
            root.add("report", parseObjectOrEmpty(sessionStore.getInformeLaboratorioJson(asignacionId)));
            root.add("device", buildDeviceJson());

            return root;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    private JsonObject parseObjectOrEmpty(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JsonObject();
        }

        try {
            JsonElement element = JsonParser.parseString(json);

            if (element != null && element.isJsonObject()) {
                return element.getAsJsonObject();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JsonObject();
    }

    private JsonArray parseArrayOrEmpty(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JsonArray();
        }

        try {
            JsonElement element = JsonParser.parseString(json);

            if (element != null && element.isJsonArray()) {
                return element.getAsJsonArray();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JsonArray();
    }

    private JsonObject buildDeviceJson() {
        JsonObject device = new JsonObject();

        device.addProperty("platform", "android");
        device.addProperty("manufacturer", android.os.Build.MANUFACTURER);
        device.addProperty("model", android.os.Build.MODEL);
        device.addProperty("brand", android.os.Build.BRAND);
        device.addProperty("android_version", android.os.Build.VERSION.RELEASE);
        device.addProperty("sdk_int", android.os.Build.VERSION.SDK_INT);
        device.addProperty("app_context", "fisikapp_movil");

        return device;
    }

    // Helper de estado
    private boolean laboratorioYaEntregado() {
        return asignacionId > 0
                && sessionStore.isEntregaEnviada(asignacionId);
    }

}