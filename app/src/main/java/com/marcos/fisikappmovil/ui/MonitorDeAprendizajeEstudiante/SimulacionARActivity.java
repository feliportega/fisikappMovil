package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.ui.UnityAR.ResultadoUnityActivity;

// import com.marcos.fisikappmovil.ui.UnityAR.UnityArActivity;

import android.webkit.WebView;

import com.marcos.fisikappmovil.ui.common.KatexWebViewRenderer;

import org.json.JSONArray;

import org.json.JSONObject;

public class SimulacionARActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTituloAr;
    private TextView tvDescripcionAr;
    private TextView tvSubtituloAr;
    private WebView webFormulaAr;
    private TextView tvIntentosAr;
    private TextView tvObjetivoAr;
    private Button btnIniciarAr;

    private LaboratorioSessionStore sessionStore;
    private TokenManager tokenManager;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    private String stepId;
    private String labKey;
    private String unitySceneName;
    private String simulationEndpoint;

    private String displayName = "Simulación AR";
    private String exerciseId = "EX-DEFAULT";

    private String simulationTitle = "Práctica simulada AR";
    private String simulationDescription = "Realiza la simulación en realidad aumentada.";

    private String formulaName = "Fórmula aplicada";
    private String formulaExpression = "";
    private String formulaDescription = "";

    private String currentStartedAt;

    private String currentRequestId;
    private String currentRunId;

    private boolean arCompleted = false;
    private boolean hitTarget = false;
    private int remainingAttempts = 4;
    private String lastResultStatus = "NO_REALIZADO";
    private int maxAttempts = 4;
    private int usedAttempts = 0;

    private final ActivityResultLauncher<Intent> unityLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String unityResultJson = result.getData().getStringExtra(ResultadoUnityActivity.EXTRA_UNITY_RESULT);

                            if (unityResultJson == null || unityResultJson.trim().isEmpty()) {
                                Toast.makeText(this, "Unity no devolvió resultados.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            abrirResultadoUnity(unityResultJson);
                        }
                    }
            );


    private final ActivityResultLauncher<Intent> resultadoUnityLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        cargarEstadoArGuardado();
                        debugEstadoAr("REGRESO_DE_RESULTADO_UNITY");
                        pintarDatos();

                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = new Intent();
                            data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulacion_ar);

        sessionStore = new LaboratorioSessionStore(this);
        tokenManager = new TokenManager(this);

        readExtras();
        cargarConfiguracionSimulacionDesdeJson();

        initViews();
        initListeners();

        cargarEstadoArGuardado();
        debugEstadoAr("DESPUES_CARGAR_ESTADO");
        pintarDatos();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sessionStore != null) {
            cargarEstadoArGuardado();
            debugEstadoAr("ON_RESUME_SIMULACION_AR");
            pintarDatos();
        }
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);

        stepId = intent.getStringExtra("STEP_ID");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackSimulacionAr);
        tvTituloAr = findViewById(R.id.tvTituloAr);
        tvSubtituloAr = findViewById(R.id.tvSubtituloAr);
        tvDescripcionAr = findViewById(R.id.tvDescripcionAr);
        webFormulaAr = findViewById(R.id.webFormulaAr);
        tvIntentosAr = findViewById(R.id.tvIntentosAr);
        tvObjetivoAr = findViewById(R.id.tvObjetivoAr);
        btnIniciarAr = findViewById(R.id.btnIniciarAr);

        KatexWebViewRenderer.configure(webFormulaAr);
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnIniciarAr.setOnClickListener(v -> iniciarUnity());
    }

    private void pintarDatos() {

        debugEstadoAr("ANTES_PINTAR_DATOS");

        tvTituloAr.setText(simulationTitle);
        tvSubtituloAr.setText("Unity / " + safe(labKey));

        tvDescripcionAr.setText(
                safe(simulationDescription).isEmpty()
                        ? "Realiza la simulación en realidad aumentada."
                        : simulationDescription
        );

        KatexWebViewRenderer.render(webFormulaAr, formulaExpression);

        tvIntentosAr.setText(
                "Intentos usados: " + usedAttempts + "/" + maxAttempts +
                        "\nIntentos disponibles: " + remainingAttempts
        );

        if ("NO_REALIZADO".equalsIgnoreCase(lastResultStatus)) {
            tvObjetivoAr.setText(
                    "Estado: no realizado.\n\n" +
                            "Simulación: " + safe(labKey) + "\n" +
                            "Escena: " + safe(unitySceneName) + "\n\n" +
                            "Fórmula: " + safe(formulaName) + "\n" +
                            safe(formulaDescription)
            );

            btnIniciarAr.setEnabled(true);
            btnIniciarAr.setText("Iniciar práctica AR");
            return;
        }

        if (arCompleted) {
            tvObjetivoAr.setText(
                    "Estado: finalizado.\n" +
                            "Impactó el target: " + (hitTarget ? "Sí" : "No") + "\n" +
                            "Intentos usados: " + usedAttempts + "/" + maxAttempts + "\n\n" +
                            "La práctica AR ya fue finalizada y guardada."
            );

            btnIniciarAr.setEnabled(false);
            btnIniciarAr.setText("Práctica AR finalizada");
            return;
        }

        if (remainingAttempts > 0) {
            tvObjetivoAr.setText(
                    "Estado: incompleto.\n" +
                            "Impactó el target: " + (hitTarget ? "Sí" : "No") + "\n" +
                            "Intentos usados: " + usedAttempts + "/" + maxAttempts + "\n" +
                            "Intentos disponibles: " + remainingAttempts + "\n\n" +
                            "Puedes continuar la práctica AR."
            );

            btnIniciarAr.setEnabled(true);
            btnIniciarAr.setText("Continuar práctica AR");
            return;
        }

        tvObjetivoAr.setText(
                "Estado: incompleto.\n" +
                        "No quedan intentos disponibles."
        );

        btnIniciarAr.setEnabled(false);
        btnIniciarAr.setText("Sin intentos disponibles");
    }

    private void iniciarUnity() {
        if (arCompleted) {
            Toast.makeText(this, "La práctica AR ya fue completada.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (remainingAttempts <= 0) {
            Toast.makeText(this, "No quedan intentos disponibles.", Toast.LENGTH_LONG).show();
            return;
        }

        String json = buildUnityConfigJson();

        if (json == null) {
            Toast.makeText(this, "No se pudo preparar la configuración AR.", Toast.LENGTH_LONG).show();
            return;
        }

        sessionStore.saveUnityLaunchContext(
                currentRunId,
                currentRequestId,
                asignacionId,
                laboratorioId,
                grupoId,
                ordenPaso
        );

        sessionStore.saveUnityStartedAt(asignacionId, currentStartedAt);

        // Comentar para deshabilitar Unity

        //Intent intent = new Intent(this, UnityArActivity.class);
        //intent.putExtra(UnityArActivity.EXTRA_EXERCISE_DATA, json);

        //unityLauncher.launch(intent);
    }

    private String buildUnityConfigJson() {
        try {
            JSONObject root = new JSONObject();

            root.put("schemaVersion", 1);

            currentRequestId = "REQ-ASG-" + asignacionId + "-LAB-" + laboratorioId;
            currentRunId = "RUN-ASG-" + asignacionId + "-" + System.currentTimeMillis();
            currentStartedAt = obtenerFechaActualUtc();

            root.put("requestId", currentRequestId);
            root.put("runId", currentRunId);
            root.put("startedAt", currentStartedAt);

            JSONObject scene = new JSONObject();
            scene.put("labKey", labKey);
            scene.put("unitySceneName", unitySceneName);
            scene.put("displayName", displayName);
            root.put("scene", scene);

            JSONObject participant = new JSONObject();
            participant.put("participantId", "STUDENT-" + tokenManager.getUserEmail());
            participant.put("displayName", tokenManager.getUserName() != null ? tokenManager.getUserName() : "Estudiante");
            root.put("participant", participant);

            JSONObject context = new JSONObject();
            context.put("organizationName", "Institución");
            context.put("courseName", "Física");
            context.put("groupName", "Grupo " + grupoId);
            root.put("context", context);

            JSONObject exercise = new JSONObject();
            exercise.put("exerciseId", exerciseId);
            exercise.put("maxAttempts", maxAttempts);
            exercise.put("allowResume", true);
            root.put("exercise", exercise);

            JSONObject options = new JSONObject();
            options.put("language", "es");
            options.put("showProjectileCameraOption", true);
            options.put("showTrajectoryPreview", true);
            root.put("options", options);

            return root.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void abrirResultadoUnity(String unityResultJson) {
        Intent intent = new Intent(this, ResultadoUnityActivity.class);

        intent.putExtra(ResultadoUnityActivity.EXTRA_UNITY_RESULT, unityResultJson);
        intent.putExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(PasosLaboratorio.EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);

        resultadoUnityLauncher.launch(intent);
    }

    private void cargarEstadoArGuardado() {
        String jsonGuardado = sessionStore.getUnityResultJson(asignacionId);

        android.util.Log.d("AR_DEBUG", "Leyendo JSON guardado para asignacionId=" + asignacionId);
        android.util.Log.d("AR_DEBUG", "jsonGuardado=" + jsonGuardado);

        if (jsonGuardado == null || jsonGuardado.trim().isEmpty()) {
            arCompleted = false;
            hitTarget = false;
            usedAttempts = 0;
            remainingAttempts = maxAttempts;
            lastResultStatus = "NO_REALIZADO";
            return;
        }

        try {
            JSONObject json = new JSONObject(jsonGuardado);

            hitTarget = json.optBoolean("hitTarget", false);
            usedAttempts = json.optInt("usedAttempts", 0);
            remainingAttempts = json.optInt("remainingAttempts", maxAttempts - usedAttempts);
            lastResultStatus = json.optString("resultStatus", "SIN_ESTADO");

            boolean completedFromJson = json.optBoolean("completed", false);

            arCompleted = completedFromJson || remainingAttempts <= 0;

        } catch (Exception e) {
            e.printStackTrace();

            arCompleted = false;
            hitTarget = false;
            usedAttempts = 0;
            remainingAttempts = maxAttempts;
            lastResultStatus = "ERROR_LECTURA";
        }
    }

    private void cargarConfiguracionSimulacionDesdeJson() {
        String json = sessionStore.getMobileResourceJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            usarConfiguracionFallback();
            return;
        }

        try {
            JSONObject root = new JSONObject(json);

            JSONObject resource = root.optJSONObject("resource");
            if (resource != null) {
                displayName = resource.optString("title", displayName);
                simulationTitle = displayName;
                simulationDescription = resource.optString(
                        "summary",
                        "Realiza la simulación en realidad aumentada."
                );
            }

            JSONArray steps = root.optJSONArray("steps");

            if (steps == null || steps.length() == 0) {
                usarConfiguracionFallback();
                return;
            }

            JSONObject simulationStep = findSimulationStep(steps);

            if (simulationStep != null) {
                //simulationTitle = simulationStep.optString("title", simulationTitle);
                simulationTitle = simulationStep.optString("intro_title", simulationTitle);

                JSONObject simulationRef = simulationStep.optJSONObject("simulation_ref");

                if (simulationRef != null) {
                    simulationEndpoint = simulationRef.optString("endpoint", "");
                    labKey = simulationRef.optString("lab_key", "");
                    unitySceneName = resolverUnityScenePorLabKey(labKey);
                }
            }

            cargarFormulaDesdeSteps(steps);

            if (labKey == null || labKey.trim().isEmpty()) {
                usarConfiguracionFallback();
            }

            android.util.Log.d(
                    "SIM_AR_CONFIG",
                    "labKey=" + labKey
                            + " | unitySceneName= " + unitySceneName
                            + " | endpoint= " + simulationEndpoint
                            + " | formula= " + formulaExpression
                            + " | title= " +simulationTitle
            );

        } catch (Exception e) {
            android.util.Log.e("SIM_AR_CONFIG", "Error leyendo configuración AR", e);
            usarConfiguracionFallback();
        }
    }

    private JSONObject findSimulationStep(JSONArray steps) {
        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.optJSONObject(i);
            if (step == null) continue;

            String type = step.optString("type", "");
            String id = step.optString("id", "");
            int order = step.optInt("order", -1);

            if (stepId != null && stepId.equals(id)) {
                return step;
            }

            if ("SIMULATION_AR".equalsIgnoreCase(type)) {
                return step;
            }

            if (ordenPaso > 0 && order == ordenPaso) {
                return step;
            }
        }

        return null;
    }

    private void cargarFormulaDesdeSteps(JSONArray steps) {
        if (steps == null) return;

        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.optJSONObject(i);
            if (step == null) continue;

            String type = step.optString("type", "");

            if (!"FORMULAS".equalsIgnoreCase(type)) {
                continue;
            }

            JSONArray formulas = step.optJSONArray("formulas");

            if (formulas == null || formulas.length() == 0) {
                return;
            }

            JSONObject formula = formulas.optJSONObject(0);

            if (formula == null) {
                return;
            }

            formulaName = formula.optString("name", "Fórmula aplicada");
            formulaExpression = formula.optString("expression", "");
            formulaDescription = formula.optString("description", "");

            return;
        }
    }

    private String resolverUnityScenePorLabKey(String labKey) {
        if (labKey == null) return "ParabolicMotionLab";

        switch (labKey) {
            case "PARABOLIC-001":
                return "ParabolicMotionLab";

            case "HOOKE-001":
                return "HookeLawLab";

            case "MRUV-001":
                return "MruvLab";

            case "FREE-FALL-001":
                return "FreeFallLab";

            default:
                return "ParabolicMotionLab";
        }
    }

    private void usarConfiguracionFallback() {
        if (labKey == null || labKey.trim().isEmpty()) {
            labKey = "PARABOLIC-001";
        }

        if (unitySceneName == null || unitySceneName.trim().isEmpty()) {
            unitySceneName = resolverUnityScenePorLabKey(labKey);
        }

        if (simulationEndpoint == null) {
            simulationEndpoint = "";
        }

        android.util.Log.w(
                "SIM_AR_CONFIG",
                "Usando configuración fallback: labKey=" + labKey
                        + " | scene=" + unitySceneName
        );
    }

    private void debugEstadoAr(String punto) {
        String jsonGuardado = sessionStore.getUnityResultJson(asignacionId);

        android.util.Log.d("AR_DEBUG", "========== " + punto + " ==========");
        android.util.Log.d("AR_DEBUG", "asignacionId=" + asignacionId);
        android.util.Log.d("AR_DEBUG", "laboratorioId=" + laboratorioId);
        android.util.Log.d("AR_DEBUG", "grupoId=" + grupoId);
        android.util.Log.d("AR_DEBUG", "ordenPaso=" + ordenPaso);
        android.util.Log.d("AR_DEBUG", "jsonGuardado=" + jsonGuardado);
        android.util.Log.d("AR_DEBUG", "arCompleted=" + arCompleted);
        android.util.Log.d("AR_DEBUG", "hitTarget=" + hitTarget);
        android.util.Log.d("AR_DEBUG", "usedAttempts=" + usedAttempts);
        android.util.Log.d("AR_DEBUG", "remainingAttempts=" + remainingAttempts);
        android.util.Log.d("AR_DEBUG", "maxAttempts=" + maxAttempts);
        android.util.Log.d("AR_DEBUG", "lastResultStatus=" + lastResultStatus);
    }

    //Helper
    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String obtenerFechaActualUtc() {
        java.text.SimpleDateFormat format =
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);

        format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        return format.format(new java.util.Date());
    }
}