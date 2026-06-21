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
import com.marcos.fisikappmovil.ui.UnityAR.UnityArActivity;

import org.json.JSONObject;

public class SimulacionARActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTituloAr;
    private TextView tvDescripcionAr;
    private TextView tvFormulaAr;
    private TextView tvIntentosAr;
    private TextView tvObjetivoAr;
    private Button btnIniciarAr;

    private LaboratorioSessionStore sessionStore;
    private TokenManager tokenManager;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    private String labKey = "PARABOLIC-001";
    private String unitySceneName = "ParabolicMotionLab";
    private String displayName = "Movimiento parabólico";
    private String exerciseId = "EX-PARABOLIC-001";

    private int maxAttempts = 3;
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
        initViews();
        initListeners();
        pintarDatos();
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);

        String extraLabKey = intent.getStringExtra("LAB_KEY");
        String extraUnityScene = intent.getStringExtra("UNITY_SCENE");

        if (extraLabKey != null && !extraLabKey.trim().isEmpty()) {
            labKey = extraLabKey;
        }

        if (extraUnityScene != null && !extraUnityScene.trim().isEmpty()) {
            unitySceneName = extraUnityScene;
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackSimulacionAr);
        tvTituloAr = findViewById(R.id.tvTituloAr);
        tvDescripcionAr = findViewById(R.id.tvDescripcionAr);
        tvFormulaAr = findViewById(R.id.tvFormulaAr);
        tvIntentosAr = findViewById(R.id.tvIntentosAr);
        tvObjetivoAr = findViewById(R.id.tvObjetivoAr);
        btnIniciarAr = findViewById(R.id.btnIniciarAr);
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnIniciarAr.setOnClickListener(v -> iniciarUnity());
    }

    private void pintarDatos() {
        tvTituloAr.setText(displayName);
        tvDescripcionAr.setText(
                "En esta práctica simulada AR configurarás potencia y ángulo de lanzamiento para impactar el objetivo."
        );

        tvFormulaAr.setText(
                "x(t) = x₀ + v₀ cos(θ)t\n" +
                        "y(t) = y₀ + v₀ sin(θ)t - 1/2 gt²"
        );

        tvIntentosAr.setText("Intentos disponibles: " + usedAttempts + "/" + maxAttempts);

        tvObjetivoAr.setText(
                "Objetivo: impactar el target antes de agotar los intentos. " +
                        "Unity devolverá distancia al objetivo, tipo de impacto e intentos usados."
        );
    }

    private void iniciarUnity() {
        String json = buildUnityConfigJson();

        if (json == null) {
            Toast.makeText(this, "No se pudo preparar la configuración AR.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, UnityArActivity.class);
        intent.putExtra(UnityArActivity.EXTRA_EXERCISE_DATA, json);

        unityLauncher.launch(intent);
    }

    private String buildUnityConfigJson() {
        try {
            JSONObject root = new JSONObject();

            root.put("schemaVersion", 1);
            root.put("requestId", "REQ-ASG-" + asignacionId + "-LAB-" + laboratorioId);
            root.put("runId", "RUN-ASG-" + asignacionId + "-" + System.currentTimeMillis());

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
}