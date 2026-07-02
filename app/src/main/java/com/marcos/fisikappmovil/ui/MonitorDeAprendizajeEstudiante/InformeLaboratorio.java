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
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;

import org.json.JSONArray;
import org.json.JSONObject;

public class InformeLaboratorio extends AppCompatActivity {

    private ImageView btnBackInformeLab;

    private TextView tvNombreLaboratorioInforme;
    private TextView tvInfoGeneralInforme;
    private TextView tvResumenArInforme;
    private TextView tvIntentosArInforme;
    private TextView tvPreguntasInforme;
    private TextView tvDatosExperimentalesInforme;
    private TextView tvComparacionInforme;

    private EditText etConclusionesInforme;
    private Button btnGuardarInforme;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_laboratorio);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        initListeners();

        pintarInforme();
        cargarConclusionesGuardadas();
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);
    }

    private void initViews() {
        btnBackInformeLab = findViewById(R.id.btnBackInformeLab);

        tvNombreLaboratorioInforme = findViewById(R.id.tvNombreLaboratorioInforme);
        tvInfoGeneralInforme = findViewById(R.id.tvInfoGeneralInforme);
        tvResumenArInforme = findViewById(R.id.tvResumenArInforme);
        tvIntentosArInforme = findViewById(R.id.tvIntentosArInforme);
        tvPreguntasInforme = findViewById(R.id.tvPreguntasInforme);
        tvDatosExperimentalesInforme = findViewById(R.id.tvDatosExperimentalesInforme);
        tvComparacionInforme = findViewById(R.id.tvComparacionInforme);

        etConclusionesInforme = findViewById(R.id.etConclusionesInforme);
        btnGuardarInforme = findViewById(R.id.btnGuardarInforme);
    }

    private void initListeners() {
        btnBackInformeLab.setOnClickListener(v -> finish());
        btnGuardarInforme.setOnClickListener(v -> guardarInforme());
    }

    private void pintarInforme() {
        tvNombreLaboratorioInforme.setText("Laboratorio de Tiro Parabólico");
        tvInfoGeneralInforme.setText("Física · Grupo " + grupoId + " · Asignación " + asignacionId);

        pintarResumenAr();
        pintarPreguntas();
        pintarDatosExperimentales();
        pintarComparacion();
    }

    private void pintarResumenAr() {
        String unityJson = sessionStore.getUnityResultJson(asignacionId);

        if (unityJson == null || unityJson.trim().isEmpty()) {
            tvResumenArInforme.setText("No hay resultado AR guardado.");
            tvIntentosArInforme.setText("Sin intentos registrados.");
            return;
        }

        try {
            JSONObject json = new JSONObject(unityJson);

            boolean completed = json.optBoolean("completed", false);
            boolean hitTarget = json.optBoolean("hitTarget", false);

            int usedAttempts = json.optInt("usedAttempts", 0);
            int maxAttempts = json.optInt("maxAttempts", 0);
            int remainingAttempts = json.optInt("remainingAttempts", 0);

            double horizontalDistance = json.optDouble("horizontalDistance", 0.0);
            double verticalDistance = json.optDouble("verticalDistance", 0.0);
            double straightDistance = json.optDouble("straightDistance", 0.0);

            String resultStatus = json.optString("resultStatus", "Sin estado");
            String finishedAt = json.optString("finishedAt", "Sin fecha");

            tvResumenArInforme.setText(
                    "Estado: " + resultStatus + "\n" +
                            "Práctica AR finalizada: " + (completed ? "Sí" : "No") + "\n" +
                            "Impactó objetivo: " + (hitTarget ? "Sí" : "No") + "\n" +
                            "Intentos usados: " + usedAttempts + "/" + maxAttempts + "\n" +
                            "Intentos restantes: " + remainingAttempts + "\n" +
                            "Distancia horizontal: " + formatMetro(horizontalDistance) + "\n" +
                            "Distancia vertical: " + formatMetro(verticalDistance) + "\n" +
                            "Distancia directa: " + formatMetro(straightDistance) + "\n" +
                            "Finalizado en: " + finishedAt
            );

            tvIntentosArInforme.setText(buildAttemptsText(json.optJSONArray("attempts")));

        } catch (Exception e) {
            e.printStackTrace();
            tvResumenArInforme.setText("No se pudo leer el resultado AR.");
            tvIntentosArInforme.setText("Error leyendo intentos AR.");
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

            builder.append("Lanzamiento ")
                    .append(item.optInt("attempt"))
                    .append("\n");

            builder.append("• Impactó: ")
                    .append(item.optBoolean("hit") ? "Sí" : "No")
                    .append("\n");

            builder.append("• Potencia: ")
                    .append(formatDecimal(item.optDouble("power", 0.0)))
                    .append("\n");

            builder.append("• Ángulo: ")
                    .append(formatDecimal(item.optDouble("angle", 0.0)))
                    .append("°\n");

            builder.append("• Tipo de impacto: ")
                    .append(item.optString("impactType", "Sin tipo"))
                    .append("\n");

            builder.append("• Distancia al objetivo: ")
                    .append(formatMetro(item.optDouble("impactDistanceToTarget", 0.0)))
                    .append("\n\n");
        }

        return builder.toString().trim();
    }

    private void pintarPreguntas() {
        String preguntasJson = sessionStore.getPreguntasJson(asignacionId);

        if (preguntasJson == null || preguntasJson.trim().isEmpty()) {
            tvPreguntasInforme.setText("No hay preguntas respondidas.");
            return;
        }

        try {
            JSONArray array = new JSONArray(preguntasJson);
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;

                builder.append("Pregunta ")
                        .append(item.optInt("id", i + 1))
                        .append(": ")
                        .append(item.optString("pregunta", "Sin pregunta"))
                        .append("\n");

                builder.append("Respuesta: ")
                        .append(item.optString("respuesta", "Sin respuesta"))
                        .append("\n\n");
            }

            tvPreguntasInforme.setText(builder.toString().trim());

        } catch (Exception e) {
            e.printStackTrace();
            tvPreguntasInforme.setText("No se pudieron leer las preguntas.");
        }
    }

    private void pintarDatosExperimentales() {
        String datosJson = sessionStore.getDatosExperimentalesJson(asignacionId);

        if (datosJson == null || datosJson.trim().isEmpty()) {
            tvDatosExperimentalesInforme.setText("No hay datos experimentales registrados.");
            return;
        }

        try {
            JSONArray array = new JSONArray(datosJson);
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;

                builder.append("• ")
                        .append(item.optString("titulo", "Dato experimental"))
                        .append(": ")
                        .append(item.optString("descripcion", "Sin descripción"))
                        .append("\n\n");
            }

            tvDatosExperimentalesInforme.setText(builder.toString().trim());

        } catch (Exception e) {
            e.printStackTrace();
            tvDatosExperimentalesInforme.setText("No se pudieron leer los datos experimentales.");
        }
    }

    private void pintarComparacion() {
        String comparacion = sessionStore.getComparacionTexto(asignacionId);

        if (comparacion == null || comparacion.trim().isEmpty()) {
            tvComparacionInforme.setText("No hay análisis comparativo guardado.");
            return;
        }

        tvComparacionInforme.setText(comparacion);
    }

    private void cargarConclusionesGuardadas() {
        String conclusiones = sessionStore.getConclusionesTexto(asignacionId);

        if (conclusiones != null && !conclusiones.trim().isEmpty()) {
            etConclusionesInforme.setText(conclusiones);
        }
    }

    private void guardarInforme() {
        String conclusiones = etConclusionesInforme.getText().toString().trim();

        if (conclusiones.isEmpty()) {
            etConclusionesInforme.setError("Escribe tus conclusiones");
            return;
        }

        if (conclusiones.length() < 20) {
            etConclusionesInforme.setError("Escribe una conclusión un poco más completa");
            return;
        }

        sessionStore.saveConclusionesTexto(asignacionId, conclusiones);

        Toast.makeText(this, "Informe guardado", Toast.LENGTH_SHORT).show();

        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }

    private String formatMetro(double value) {
        return String.format(java.util.Locale.US, "%.2f m", value);
    }

    private String formatDecimal(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }
}