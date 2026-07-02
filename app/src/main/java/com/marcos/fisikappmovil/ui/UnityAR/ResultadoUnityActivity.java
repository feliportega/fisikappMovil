package com.marcos.fisikappmovil.ui.UnityAR;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.PasosLaboratorio;

import org.json.JSONArray;
import org.json.JSONObject;

public class ResultadoUnityActivity extends AppCompatActivity {

    public static final String EXTRA_UNITY_RESULT = "unityResult";

    private TextView txtTituloResultadoUnity;
    private TextView txtEstadoUnity;
    private TextView txtResumenUnity;
    private TextView txtIntentosUnity;
    private TextView txtDistanciaUnity;
    private TextView txtDetalleIntentosUnity;
    private Button btnGuardarResultadoUnity;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    private String resultJson;
    private String runId;
    private String requestId;

    private boolean resultadoProcesado = false;
    private boolean pasoArCompletado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_unity);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        pintarResultado();

        // Punto clave: guardar automáticamente apenas llega el resultado.
        procesarResultadoInmediatamente();

        initListeners();

        // Evita volver a Unity/Simulación con botón atrás.
        getOnBackPressedDispatcher().addCallback(
                this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        irAPasosLaboratorio();
                    }
                }
        );
    }

    private void initListeners() {
        btnGuardarResultadoUnity.setText("Continuar laboratorio");
        btnGuardarResultadoUnity.setOnClickListener(v -> irAPasosLaboratorio());
    }


    private void readExtras() {
        Intent intent = getIntent();

        resultJson = intent.getStringExtra(EXTRA_UNITY_RESULT);

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);

        recuperarContextoDesdeJsonSiHaceFalta();

        android.util.Log.d("RESULT_UNITY", "asignacionId=" + asignacionId);
        android.util.Log.d("RESULT_UNITY", "laboratorioId=" + laboratorioId);
        android.util.Log.d("RESULT_UNITY", "grupoId=" + grupoId);
        android.util.Log.d("RESULT_UNITY", "ordenPaso=" + ordenPaso);
        android.util.Log.d("RESULT_UNITY", "runId=" + runId);
        android.util.Log.d("RESULT_UNITY", "requestId=" + requestId);
    }

    private void initViews() {
        txtTituloResultadoUnity = findViewById(R.id.txtTituloResultadoUnity);
        txtEstadoUnity = findViewById(R.id.txtEstadoUnity);
        txtResumenUnity = findViewById(R.id.txtResumenUnity);
        txtIntentosUnity = findViewById(R.id.txtIntentosUnity);
        txtDistanciaUnity = findViewById(R.id.txtDistanciaUnity);
        txtDetalleIntentosUnity = findViewById(R.id.txtDetalleIntentosUnity);
        btnGuardarResultadoUnity = findViewById(R.id.btnGuardarResultadoUnity);
    }

    private void pintarResultado() {
        if (resultJson == null || resultJson.trim().isEmpty()) {
            txtEstadoUnity.setText("No se recibió resultado");
            txtResumenUnity.setText("Unity no devolvió datos de la práctica.");
            txtIntentosUnity.setText("-");
            txtDistanciaUnity.setText("-");
            txtDetalleIntentosUnity.setText("");
            return;
        }

        try {
            JSONObject json = new JSONObject(resultJson);

            String labKey = json.optString("labKey", "PARABOLIC-001");
            String unityScene = json.optString("unitySceneName", "ParabolicMotionLab");

            boolean hitTarget = json.optBoolean("hitTarget", false);
            boolean completed = json.optBoolean("completed", false);

            int usedAttempts = json.optInt("usedAttempts", 0);
            int maxAttempts = json.optInt("maxAttempts", 0);
            int remainingAttempts = json.optInt("remainingAttempts", 0);

            double horizontalDistance = json.optDouble("horizontalDistance", 0);
            double verticalDistance = json.optDouble("verticalDistance", 0);
            double straightDistance = json.optDouble("straightDistance", 0);

            txtTituloResultadoUnity.setText("Resultado AR · " + labKey);

            if (hitTarget && completed) {
                txtEstadoUnity.setText("Práctica simulada completada");
            } else {
                txtEstadoUnity.setText("Práctica simulada no completada");
            }

            txtResumenUnity.setText(
                    "Escena: " + unityScene + "\n" +
                            "Impactó el target: " + (hitTarget ? "Sí" : "No") + "\n" +
                            "Estado: " + json.optString("resultStatus", "Sin estado")
            );

            txtIntentosUnity.setText(
                    "Intentos usados: " + usedAttempts + "/" + maxAttempts +
                            "\nIntentos restantes: " + remainingAttempts
            );

            txtDistanciaUnity.setText(
                    "Distancia horizontal: " + horizontalDistance + " m\n" +
                            "Distancia vertical: " + verticalDistance + " m\n" +
                            "Distancia directa: " + straightDistance + " m"
            );

            txtDetalleIntentosUnity.setText(buildAttemptsText(json.optJSONArray("attempts")));

        } catch (Exception e) {
            e.printStackTrace();
            txtEstadoUnity.setText("Error leyendo resultado");
            txtResumenUnity.setText(resultJson);
        }
    }

    private String buildAttemptsText(JSONArray attempts) {
        if (attempts == null || attempts.length() == 0) {
            return "Sin detalle de intentos.";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < attempts.length(); i++) {
            JSONObject item = attempts.optJSONObject(i);
            if (item == null) continue;

            builder.append("Intento ")
                    .append(item.optInt("attempt"))
                    .append("\n");

            builder.append("• Impactó: ")
                    .append(item.optBoolean("hit") ? "Sí" : "No")
                    .append("\n");

            builder.append("• Potencia: ")
                    .append(item.optDouble("power"))
                    .append("\n");

            builder.append("• Ángulo: ")
                    .append(item.optDouble("angle"))
                    .append("°\n");

            builder.append("• Tipo: ")
                    .append(item.optString("impactType"))
                    .append("\n");

            builder.append("• Distancia al objetivo: ")
                    .append(item.optDouble("impactDistanceToTarget"))
                    .append(" m\n\n");
        }

        return builder.toString();
    }

    private void recuperarContextoDesdeJsonSiHaceFalta() {
        if (resultJson == null || resultJson.trim().isEmpty()) {
            return;
        }

        try {
            JSONObject json = new JSONObject(resultJson);

            runId = json.optString("runId", null);
            requestId = json.optString("requestId", null);

            JSONObject context = sessionStore.getUnityLaunchContext(runId, requestId);

            if (context != null) {
                if (asignacionId == -1) {
                    asignacionId = context.optInt("asignacionId", -1);
                }

                if (laboratorioId == -1) {
                    laboratorioId = context.optInt("laboratorioId", -1);
                }

                if (grupoId == -1) {
                    grupoId = context.optInt("grupoId", -1);
                }

                if (ordenPaso == -1) {
                    ordenPaso = context.optInt("ordenPaso", -1);
                }
            }

            if (asignacionId == -1 || laboratorioId == -1) {
                recuperarIdsDesdeRequestId(requestId);
            }

            // En tu mock actual el paso AR es el 5.
            if (ordenPaso == -1) {
                ordenPaso = 5;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recuperarIdsDesdeRequestId(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            return;
        }

        try {
            String[] parts = requestId.split("-");

            for (int i = 0; i < parts.length; i++) {
                if ("ASG".equalsIgnoreCase(parts[i]) && i + 1 < parts.length) {
                    asignacionId = Integer.parseInt(parts[i + 1]);
                }

                if ("LAB".equalsIgnoreCase(parts[i]) && i + 1 < parts.length) {
                    laboratorioId = Integer.parseInt(parts[i + 1]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void guardarYCompletar() {
        boolean completarPaso = false;

        if (resultJson != null && !resultJson.trim().isEmpty()) {
            sessionStore.saveUnityResultJson(asignacionId, resultJson);

            try {
                JSONObject json = new JSONObject(resultJson);
                completarPaso = debeCompletarPasoAr(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        android.util.Log.d("RESULT_UNITY", "guardarYCompletar asignacionId=" + asignacionId);
        android.util.Log.d("RESULT_UNITY", "guardarYCompletar ordenPaso=" + ordenPaso);
        android.util.Log.d("RESULT_UNITY", "guardarYCompletar completarPaso=" + completarPaso);

        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);

        if (completarPaso) {
            if (asignacionId != -1 && ordenPaso != -1) {
                sessionStore.completarPasoYDesbloquearSiguiente(asignacionId, ordenPaso);
            }

            setResult(RESULT_OK, data);
        } else {
            setResult(RESULT_CANCELED, data);
        }

        finish();
    }

    private boolean debeCompletarPasoAr(JSONObject json) {
        boolean completed = json.optBoolean("completed", false);
        int remainingAttempts = json.optInt("remainingAttempts", -1);
        String resultStatus = json.optString("resultStatus", "");
        String exitReason = json.optString("exitReason", "");

        android.util.Log.d("RESULT_UNITY", "completed=" + completed);
        android.util.Log.d("RESULT_UNITY", "remainingAttempts=" + remainingAttempts);
        android.util.Log.d("RESULT_UNITY", "resultStatus=" + resultStatus);
        android.util.Log.d("RESULT_UNITY", "exitReason=" + exitReason);

        if (completed) return true;

        if ("completed".equalsIgnoreCase(resultStatus)) return true;

        if (remainingAttempts == 0) return true;

        if ("max_attempts".equalsIgnoreCase(exitReason)
                || "MaxAttemptsReached".equalsIgnoreCase(exitReason)) {
            return true;
        }

        return false;
    }

    private void procesarResultadoInmediatamente() {
        if (resultadoProcesado) {
            return;
        }

        resultadoProcesado = true;

        if (resultJson == null || resultJson.trim().isEmpty()) {
            txtEstadoUnity.setText("No se recibió resultado desde Unity");
            pasoArCompletado = false;
            return;
        }

        try {
            JSONObject json = new JSONObject(resultJson);

            pasoArCompletado = debeCompletarPasoAr(json);

            if (asignacionId != -1) {
                sessionStore.saveUnityResultJson(asignacionId, resultJson);
            }

            if (pasoArCompletado && asignacionId != -1 && ordenPaso != -1) {
                sessionStore.completarPasoYDesbloquearSiguiente(asignacionId, ordenPaso);
            }

            android.util.Log.d("RESULT_UNITY", "AUTO_SAVE asignacionId=" + asignacionId);
            android.util.Log.d("RESULT_UNITY", "AUTO_SAVE ordenPaso=" + ordenPaso);
            android.util.Log.d("RESULT_UNITY", "AUTO_SAVE pasoArCompletado=" + pasoArCompletado);

        } catch (Exception e) {
            e.printStackTrace();
            txtEstadoUnity.setText("Error guardando resultado AR");
        }
    }

    private void irAPasosLaboratorio() {
        Intent intent = new Intent(this, PasosLaboratorio.class);

        intent.putExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(PasosLaboratorio.EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);

        /*
         * CLEAR_TOP:
         * Si PasosLaboratorio ya está debajo en la pila,
         * elimina SimulacionARActivity, UnityArActivity y ResultadoUnityActivity.
         *
         * SINGLE_TOP:
         * Reutiliza PasosLaboratorio si ya existe arriba después del CLEAR_TOP.
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
    }

}