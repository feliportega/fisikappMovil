package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.repository.SimulacionRepository;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.remote.response.MobileSimulationResponse;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.ui.UnityAR.ResultadoUnityActivity;
import com.marcos.fisikappmovil.ui.common.ContentStateView;

// Comentar el import para deshabilitar Unity
// Comentar la clase UnityAR.UnityArActivity.java
// Comentar Unity library desde .Build.gradle.kts (:app)
// Comentar Unity library desde setting.gradle.kts (FisicaappMovil)

//import com.marcos.fisikappmovil.ui.UnityAR.UnityArActivity;

import android.webkit.WebView;

import com.marcos.fisikappmovil.ui.common.KatexWebViewRenderer;

import android.graphics.Color;


import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

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

    private int simulationId = -1;

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

    private ScrollView contentSimulacionAr;
    private ContentStateView stateSimulacionAr;

    private TextView tvInstructionsTitleAr;
    private LinearLayout layoutInstructionsAr;

    private SimulacionRepository simulacionRepository;

    private String introTitle = "Práctica simulada AR";
    private String introText = "Cargando descripción de la simulación...";

    private final List<String> instructions = new ArrayList<>();

    private final Gson gson = new Gson();

    private boolean simulationConfigLoaded = false;

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
        simulacionRepository = new SimulacionRepository();

        readExtras();
        initViews();
        initListeners();

        resolverSimulationRefDesdeMobileResource();

        boolean loadedFromCache = cargarConfiguracionSimulacionDesdeCache();

        cargarConfiguracionSimulacionDesdeJson();
        cargarConfiguracionSimulacionDesdeExtrasOCache();
        cargarEstadoArGuardado();
        pintarDatos();

        consultarConfiguracionSimulacionBackend(!loadedFromCache);
        debugEstadoAr("DESPUES_CARGAR_ESTADO");
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
        simulationEndpoint = intent.getStringExtra("SIMULATION_ENDPOINT");

        String extraLabKey = intent.getStringExtra("LAB_KEY");
        if (extraLabKey != null && !extraLabKey.trim().isEmpty()) {
            labKey = extraLabKey;
        }

        simulationId = extraerSimulationIdDesdeEndpoint(simulationEndpoint);
    }

    private void initViews() {
        contentSimulacionAr = findViewById(R.id.contentSimulacionAr);
        stateSimulacionAr = new ContentStateView(findViewById(R.id.stateSimulacionAr));

        btnBack = findViewById(R.id.btnBackSimulacionAr);

        tvTituloAr = findViewById(R.id.tvTituloAr);
        tvSubtituloAr = findViewById(R.id.tvSubtituloAr);
        tvDescripcionAr = findViewById(R.id.tvDescripcionAr);

        webFormulaAr = findViewById(R.id.webFormulaAr);
        KatexWebViewRenderer.configure(webFormulaAr);

        tvInstructionsTitleAr = findViewById(R.id.tvInstructionsTitleAr);
        layoutInstructionsAr = findViewById(R.id.layoutInstructionsAr);

        tvIntentosAr = findViewById(R.id.tvIntentosAr);
        tvObjetivoAr = findViewById(R.id.tvObjetivoAr);
        btnIniciarAr = findViewById(R.id.btnIniciarAr);

        stateSimulacionAr.hide();
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnIniciarAr.setOnClickListener(v -> iniciarUnity());
    }

    private void pintarDatos() {
        debugEstadoAr("ANTES_PINTAR_DATOS");

        tvTituloAr.setText(
                notEmpty(displayName)
                        ? "Unity / " + displayName
                        : "Unity / Realidad aumentada"
        );

        tvSubtituloAr.setText(
                notEmpty(introTitle)
                        ? introTitle
                        : "Práctica simulada AR"
        );

        tvDescripcionAr.setText(
                notEmpty(introText)
                        ? introText
                        : "Realiza la simulación en realidad aumentada."
        );

        KatexWebViewRenderer.render(webFormulaAr, formulaExpression);

        renderInstructions();

        tvIntentosAr.setText(
                "Intentos usados: " + usedAttempts + "/" + maxAttempts +
                        "\nIntentos disponibles: " + remainingAttempts
        );

        if ("NO_REALIZADO".equalsIgnoreCase(lastResultStatus)) {
            tvObjetivoAr.setText(
                    "Estado: no realizado.\n\n" +
                            "Simulación: " + safe(labKey) + "\n" +
                            "Escena Unity: " + safe(unitySceneName) + "\n\n" +
                            "Objetivo: realiza la práctica AR siguiendo las instrucciones."
            );

            btnIniciarAr.setEnabled(simulationConfigLoaded);
            btnIniciarAr.setText(
                    simulationConfigLoaded
                            ? "Iniciar práctica AR"
                            : "Cargando simulación..."
            );
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

            btnIniciarAr.setEnabled(simulationConfigLoaded);
            btnIniciarAr.setText(
                    simulationConfigLoaded
                            ? "Continuar práctica AR"
                            : "Cargando simulación..."
            );
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

        // ================= BYPASS TEMPORAL UNITY =================
        // Usar esto cuando Unity esté deshabilitado para que el equipo pueda compilar.
        // Para usar Unity real: comentar este bloque y habilitar el Intent real de Unity.
                try {
                    org.json.JSONObject fakeResult = new org.json.JSONObject();

                    fakeResult.put("schemaVersion", 1);
                    fakeResult.put("requestId", currentRequestId);
                    fakeResult.put("runId", currentRunId);
                    fakeResult.put("startedAt", currentStartedAt);
                    fakeResult.put("finishedAt", obtenerFechaActualUtc());

                    fakeResult.put("labKey", labKey);
                    fakeResult.put("unitySceneName", unitySceneName);
                    fakeResult.put("exerciseId", exerciseId);

                    fakeResult.put("participantId", "STUDENT-" + tokenManager.getUserEmail());
                    fakeResult.put("participantName", tokenManager.getUserName() != null
                            ? tokenManager.getUserName()
                            : "Estudiante");

                    fakeResult.put("organizationName", "Institución");
                    fakeResult.put("courseName", "Física");
                    fakeResult.put("groupName", "Grupo " + grupoId);

                    fakeResult.put("horizontalDistance", 1.25);
                    fakeResult.put("verticalDistance", 0.10);
                    fakeResult.put("straightDistance", 1.26);

                    fakeResult.put("hitTarget", true);
                    fakeResult.put("maxAttempts", maxAttempts);
                    fakeResult.put("usedAttempts", 1);
                    fakeResult.put("remainingAttempts", Math.max(maxAttempts - 1, 0));
                    fakeResult.put("completed", true);
                    fakeResult.put("resultStatus", "completed");
                    fakeResult.put("exitReason", "UNITY_BYPASS_TEMPORAL");

                    org.json.JSONArray attempts = new org.json.JSONArray();

                    org.json.JSONObject attempt = new org.json.JSONObject();
                    attempt.put("attempt", 1);
                    attempt.put("hit", true);
                    attempt.put("power", 5.0);
                    attempt.put("angle", 45.0);
                    attempt.put("impactDistanceToTarget", 0.02);
                    attempt.put("impactHorizontalDistance", 1.25);
                    attempt.put("impactHeightDifference", 0.10);
                    attempt.put("impactType", "HitTarget");

                    attempts.put(attempt);

                    fakeResult.put("attempts", attempts);

                    abrirResultadoUnity(fakeResult.toString());
                    return;

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "No se pudo simular el resultado Unity.", Toast.LENGTH_LONG).show();
                    return;
                }
        // ================= FIN BYPASS TEMPORAL UNITY =================

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

                    labKey = simulationRef.optString(
                            "lab_key",
                            simulationRef.optString("labKey", "")
                    );

                    unitySceneName = simulationRef.optString(
                            "unity_scene_name",
                            simulationRef.optString("unitySceneName", resolverUnityScenePorLabKey(labKey))
                    );

                    displayName = simulationRef.optString(
                            "display_name",
                            simulationRef.optString("displayName", displayName)
                    );

                    introTitle = simulationRef.optString(
                            "intro_title",
                            simulationRef.optString("introTitle", simulationTitle)
                    );

                    introText = simulationRef.optString(
                            "intro_text",
                            simulationRef.optString("introText", simulationDescription)
                    );

                    maxAttempts = simulationRef.optInt("max_attempts", maxAttempts);

                    cargarInstructionsDesdeJson(simulationRef.optJSONArray("instructions"));
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

    private void resolverSimulationRefDesdeMobileResource() {
        if (simulationId > 0 && notEmpty(simulationEndpoint)) {
            return;
        }

        String json = sessionStore.getMobileResourceJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            android.util.Log.d("SIM_AR_CONFIG", "No hay mobile_resource_json para resolver simulation_ref.");
            return;
        }

        try {
            org.json.JSONObject root = new org.json.JSONObject(json);
            org.json.JSONArray steps = root.optJSONArray("steps");

            if (steps == null) return;

            for (int i = 0; i < steps.length(); i++) {
                org.json.JSONObject step = steps.optJSONObject(i);
                if (step == null) continue;

                String id = step.optString("id", "");
                String type = step.optString("type", "");
                int order = step.optInt("order", -1);

                boolean isTargetStep =
                        ("SIMULATION_AR".equalsIgnoreCase(type))
                                || (stepId != null && stepId.equals(id))
                                || (ordenPaso > 0 && ordenPaso == order);

                if (!isTargetStep) continue;

                org.json.JSONObject simulationRef = step.optJSONObject("simulation_ref");

                if (simulationRef == null) {
                    return;
                }

                if (!notEmpty(simulationEndpoint)) {
                    simulationEndpoint = simulationRef.optString("endpoint", "");
                }

                if (!notEmpty(labKey)) {
                    labKey = simulationRef.optString("lab_key", "");
                }

                simulationId = extraerSimulationIdDesdeEndpoint(simulationEndpoint);

                android.util.Log.d(
                        "SIM_AR_CONFIG",
                        "simulation_ref resuelto desde cache: endpoint="
                                + simulationEndpoint
                                + " simulationId="
                                + simulationId
                                + " labKey="
                                + labKey
                );

                return;
            }

        } catch (Exception e) {
            android.util.Log.e("SIM_AR_CONFIG", "Error resolviendo simulation_ref", e);
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

    private void cargarInstructionsDesdeJson(JSONArray array) {
        instructions.clear();

        if (array == null || array.length() == 0) {
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            String item = array.optString(i, "");

            if (item != null && !item.trim().isEmpty()) {
                instructions.add(item.trim());
            }
        }
    }

    private void renderInstructions() {
        layoutInstructionsAr.removeAllViews();

        if (instructions.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No hay instrucciones configuradas para esta simulación.");
            empty.setTextColor(Color.parseColor("#64748B"));
            empty.setTextSize(14);
            empty.setLineSpacing(4f, 1.1f);
            layoutInstructionsAr.addView(empty);
            return;
        }

        for (int i = 0; i < instructions.size(); i++) {
            TextView itemView = new TextView(this);

            itemView.setText((i + 1) + ". " + instructions.get(i));
            itemView.setTextColor(Color.parseColor("#334155"));
            itemView.setTextSize(14);
            itemView.setLineSpacing(4f, 1.15f);
            itemView.setPadding(0, 0, 0, dpToPx(8));

            layoutInstructionsAr.addView(itemView);
        }
    }
    private void cargarConfiguracionSimulacionDesdeExtrasOCache() {
        if (simulationId > 0) {
            String cached = sessionStore.getSimulationConfigJson(simulationId);

            if (cached != null && !cached.trim().isEmpty()) {
                try {
                    MobileSimulationResponse cachedResponse =
                            new com.google.gson.Gson().fromJson(
                                    cached,
                                    MobileSimulationResponse.class
                            );

                    aplicarSimulationResponse(cachedResponse);

                    android.util.Log.d(
                            "SIM_AR_CONFIG",
                            "Configuración cargada desde cache simulationId=" + simulationId
                    );

                    return;

                } catch (Exception e) {
                    android.util.Log.e("SIM_AR_CONFIG", "Error leyendo cache de simulación", e);
                }
            }
        }

        // Fallback mínimo mientras carga el backend.
        if (labKey == null || labKey.trim().isEmpty()) {
            labKey = "LAB-GENERICO";
        }

        if (unitySceneName == null || unitySceneName.trim().isEmpty()) {
            unitySceneName = "";
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "Simulación AR";
        }

        introTitle = displayName;
        introText = "Cargando configuración de simulación...";
    }

    private void aplicarSimulationResponse(MobileSimulationResponse response) {
        if (response == null) return;

        if (notEmpty(response.getLabKey())) {
            labKey = response.getLabKey();
        }

        if (notEmpty(response.getUnitySceneName())) {
            unitySceneName = response.getUnitySceneName();
        }

        if (notEmpty(response.getDisplayName())) {
            displayName = response.getDisplayName();
            simulationTitle = response.getDisplayName();
        }

        if (notEmpty(response.getIntroTitle())) {
            introTitle = response.getIntroTitle();
        } else if (notEmpty(displayName)) {
            introTitle = displayName;
        }

        if (notEmpty(response.getIntroText())) {
            introText = response.getIntroText();
            simulationDescription = response.getIntroText();
        }

        if (response.getMaxAttempts() > 0) {
            maxAttempts = response.getMaxAttempts();
        }

        instructions.clear();

        if (response.getInstructions() != null) {
            for (String item : response.getInstructions()) {
                if (notEmpty(item)) {
                    instructions.add(item.trim());
                }
            }
        }

        if (!arCompleted && usedAttempts == 0) {
            remainingAttempts = maxAttempts;
        }
    }

    private boolean cargarConfiguracionSimulacionDesdeCache() {
        if (simulationId <= 0) {
            return false;
        }

        String cached = sessionStore.getSimulationConfigJson(simulationId);

        if (cached == null || cached.trim().isEmpty()) {
            return false;
        }

        try {
            MobileSimulationResponse response = gson.fromJson(
                    cached,
                    MobileSimulationResponse.class
            );

            aplicarSimulationResponse(response);

            simulationConfigLoaded = true;

            android.util.Log.d(
                    "SIM_AR_CONFIG",
                    "Configuración AR cargada desde cache. simulationId=" + simulationId
            );

            return true;

        } catch (Exception e) {
            android.util.Log.e("SIM_AR_CONFIG", "Error leyendo cache de simulación", e);
            return false;
        }
    }

    private void consultarConfiguracionSimulacionBackend(boolean showFullLoading) {
        if (simulationId <= 0) {
            stateSimulacionAr.showError(
                    "No se encontró la simulación",
                    "El laboratorio no tiene un endpoint de simulación AR válido.",
                    v -> consultarConfiguracionSimulacionBackend(true)
            );

            contentSimulacionAr.setVisibility(View.GONE);
            return;
        }

        String token = tokenManager.getAccessToken();

        if (token == null || token.trim().isEmpty()) {
            stateSimulacionAr.showError(
                    "Sesión no válida",
                    "No se encontró un token activo. Inicia sesión nuevamente.",
                    v -> consultarConfiguracionSimulacionBackend(true)
            );

            contentSimulacionAr.setVisibility(View.GONE);
            return;
        }

        if (showFullLoading) {
            stateSimulacionAr.showLoading(
                    "Cargando simulación",
                    "Estamos consultando la configuración de la práctica AR."
            );
            contentSimulacionAr.setVisibility(View.GONE);
        } else {
            btnIniciarAr.setEnabled(false);
            btnIniciarAr.setText("Actualizando simulación...");
        }

        simulacionRepository.getMobileSimulationConfig(
                "Bearer " + token,
                simulationId,
                new RepositoryCallback<MobileSimulationResponse>() {
                    @Override
                    public void onComplete(AppResult<MobileSimulationResponse> result) {
                        runOnUiThread(() -> {
                            if (!result.isSuccess()) {
                                manejarErrorConfiguracionSimulacion(result, showFullLoading);
                                return;
                            }

                            MobileSimulationResponse response = result.getData();

                            if (response == null) {
                                manejarErrorConfiguracionVacia(showFullLoading);
                                return;
                            }

                            aplicarSimulationResponse(response);
                            simulationConfigLoaded = true;

                            try {
                                sessionStore.saveSimulationConfigJson(
                                        simulationId,
                                        gson.toJson(response)
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            stateSimulacionAr.hide();
                            contentSimulacionAr.setVisibility(View.VISIBLE);

                            android.util.Log.d(
                                    "SIM_AR_CONFIG",
                                    "Configuración AR aplicada. instructions="
                                            + (response.getInstructions() == null
                                            ? 0
                                            : response.getInstructions().size())
                            );

                            pintarDatos();
                        });
                    }
                }
        );
    }

    private void manejarErrorConfiguracionVacia(boolean showFullLoading) {
        if (simulationConfigLoaded || !showFullLoading) {
            stateSimulacionAr.hide();
            contentSimulacionAr.setVisibility(View.VISIBLE);
            pintarDatos();

            Toast.makeText(
                    this,
                    "La configuración de simulación llegó vacía.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        stateSimulacionAr.showEmpty(
                "Simulación sin configuración",
                "El backend no entregó datos para esta práctica AR."
        );

        contentSimulacionAr.setVisibility(View.GONE);
    }

    // Helper
    private int extraerSimulationIdDesdeEndpoint(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return -1;
        }

        try {
            java.util.regex.Pattern pattern =
                    java.util.regex.Pattern.compile("/simulation/(\\d+)/?");

            java.util.regex.Matcher matcher = pattern.matcher(endpoint);

            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private void manejarErrorConfiguracionSimulacion(
            AppResult<MobileSimulationResponse> result,
            boolean showFullLoading
    ) {
        String message = result.getStatusCode() == -1
                ? "No se pudo conectar con el servidor. Revisa tu internet o intenta nuevamente."
                : result.getErrorMessage();

        if (simulationConfigLoaded || !showFullLoading) {
            stateSimulacionAr.hide();
            contentSimulacionAr.setVisibility(View.VISIBLE);
            pintarDatos();

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }

        stateSimulacionAr.showError(
                "No se pudo cargar la simulación",
                message,
                v -> consultarConfiguracionSimulacionBackend(true)
        );

        contentSimulacionAr.setVisibility(View.GONE);
    }

    private boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    private String obtenerFechaActualUtc() {
        java.text.SimpleDateFormat format =
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);

        format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        return format.format(new java.util.Date());
    }

}