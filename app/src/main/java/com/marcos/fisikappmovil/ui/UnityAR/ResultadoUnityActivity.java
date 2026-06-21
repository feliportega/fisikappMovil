package com.marcos.fisikappmovil.ui.UnityAR;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
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
    private int ordenPaso = -1;

    private String resultJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_unity);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        pintarResultado();
        initListeners();
    }

    private void readExtras() {
        Intent intent = getIntent();

        resultJson = intent.getStringExtra(EXTRA_UNITY_RESULT);
        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);
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

    private void initListeners() {
        btnGuardarResultadoUnity.setOnClickListener(v -> guardarYCompletar());
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

    private void guardarYCompletar() {
        if (resultJson != null && !resultJson.trim().isEmpty()) {
            sessionStore.saveUnityResultJson(asignacionId, resultJson);
        }

        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }
}