package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
import com.marcos.fisikappmovil.ui.common.StepCompletionOverlay;

import org.json.JSONArray;
import org.json.JSONObject;

public class ComparacionResultadosActivity extends AppCompatActivity {

    private TextView tvFuentesComparacion;
    private TextView tvResultadoAr;
    private TextView tvResumenDatosExperimentales;
    private TextView tvComparacionInstructions;
    private LinearLayout layoutComparisonFieldsContainer;

    private Button btnGuardarComparacion;

    private JSONObject comparisonObject;
    private JSONObject comparisonData = new JSONObject();
    private ImageView btnBack;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparacion_resultados);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        initListeners();

        cargarComparisonDesdeJson();
        cargarComparacionGuardada();

        pintarFuentesComparacion();
        cargarResumenUnity();
        cargarDatosPracticaExperimental();
        renderComparisonFields();
        configurarModoPantalla();
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackComparacion);

        tvFuentesComparacion = findViewById(R.id.tvFuentesComparacion);
        tvResultadoAr = findViewById(R.id.tvResultadoAr);
        tvResumenDatosExperimentales = findViewById(R.id.tvResumenDatosExperimentales);

        tvComparacionInstructions = findViewById(R.id.tvComparacionInstructions);
        layoutComparisonFieldsContainer = findViewById(R.id.layoutComparisonFieldsContainer);

        btnGuardarComparacion = findViewById(R.id.btnGuardarComparacion);
    }

    private void configurarModoPantalla() {
        if (modoSoloLectura()) {
            btnGuardarComparacion.setText("Regresar");
            setEditableRecursive(layoutComparisonFieldsContainer, false);
        } else {
            btnGuardarComparacion.setText("Guardar comparación");
            setEditableRecursive(layoutComparisonFieldsContainer, true);
        }
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnGuardarComparacion.setOnClickListener(v -> guardarAnalisisYCompletar());
    }

    private void cargarResumenUnity() {
        String unityJson = sessionStore.getUnityResultJson(asignacionId);

        if (unityJson == null || unityJson.trim().isEmpty()) {
            tvResultadoAr.setText("No hay resultado AR guardado.");
            return;
        }

        try {
            JSONObject json = new JSONObject(unityJson);

            boolean hitTarget = json.optBoolean("hitTarget", false);
            boolean completed = json.optBoolean("completed", false);

            int usedAttempts = json.optInt("usedAttempts", 0);
            int maxAttempts = json.optInt("maxAttempts", 0);
            int remainingAttempts = json.optInt("remainingAttempts", 0);

            String resultStatus = json.optString("resultStatus", "Sin estado");
            String exitReason = json.optString("exitReason", "");

            double horizontalDistance = json.optDouble("horizontalDistance", 0.0);
            double verticalDistance = json.optDouble("verticalDistance", 0.0);
            double straightDistance = json.optDouble("straightDistance", 0.0);

            tvResultadoAr.setText(
                    "Estado AR: " + resultStatus + "\n" +
                            "Práctica finalizada: " + (completed ? "Sí" : "No") + "\n" +
                            "Impactó objetivo: " + (hitTarget ? "Sí" : "No") + "\n" +
                            "Intentos usados: " + usedAttempts + "/" + maxAttempts + "\n" +
                            "Intentos restantes: " + remainingAttempts + "\n" +
                            "Distancia horizontal AR: " + formatDecimal(horizontalDistance) + "\n" +
                            "Distancia vertical AR: " + formatDecimal(verticalDistance) + "\n" +
                            "Distancia directa AR: " + formatDecimal(straightDistance) +
                            (exitReason == null || exitReason.trim().isEmpty()
                                    ? ""
                                    : "\nSalida: " + exitReason)
            );

        } catch (Exception e) {
            e.printStackTrace();
            tvResultadoAr.setText("No se pudo leer el resultado AR guardado.");
        }
    }

    private void cargarComparisonDesdeJson() {
        String json = sessionStore.getMobileResourceJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            tvComparacionInstructions.setText("No se encontró la configuración de comparación.");
            return;
        }

        try {
            JSONObject root = new JSONObject(json);
            JSONArray steps = root.optJSONArray("steps");

            if (steps == null || steps.length() == 0) {
                tvComparacionInstructions.setText("Este laboratorio no tiene pasos configurados.");
                return;
            }

            JSONObject comparisonStep = findComparisonStep(steps);

            if (comparisonStep == null) {
                tvComparacionInstructions.setText("No se encontró el paso de comparación.");
                return;
            }

            comparisonObject = comparisonStep.optJSONObject("comparison");

            if (comparisonObject == null) {
                tvComparacionInstructions.setText("El paso de comparación no tiene configuración.");
                return;
            }

            String instructions = comparisonObject.optString(
                    "instructions",
                    "Compare los resultados registrados en la práctica experimental con los resultados de la simulación AR."
            );

            tvComparacionInstructions.setText(instructions);

        } catch (Exception e) {
            e.printStackTrace();
            tvComparacionInstructions.setText("No se pudo leer la configuración de comparación.");
        }
    }

    private JSONObject findComparisonStep(JSONArray steps) {
        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.optJSONObject(i);
            if (step == null) continue;

            String type = step.optString("type", "");
            int order = step.optInt("order", -1);

            if ("COMPARISON".equalsIgnoreCase(type)) {
                return step;
            }

            if (ordenPaso > 0 && order == ordenPaso) {
                return step;
            }
        }

        return null;
    }

    private void pintarFuentesComparacion() {
        String leftSource = comparisonObject != null
                ? comparisonObject.optString("left_source", "experimental_practice")
                : "experimental_practice";

        String rightSource = comparisonObject != null
                ? comparisonObject.optString("right_source", "simulation_ar")
                : "simulation_ar";

        tvFuentesComparacion.setText(
                "Fuente experimental: " + traducirFuente(leftSource) + "\n" +
                        "Fuente simulada: " + traducirFuente(rightSource) + "\n\n" +
                        "Revisa ambas fuentes y escribe una comparación según las instrucciones del laboratorio. " +
                        "La comparación puede ser numérica, conceptual, cualitativa o de observación, según el tipo de práctica."
        );
    }

    private void cargarDatosPracticaExperimental() {
        String practicaJson = sessionStore.getPracticaExperimentalJson(asignacionId);

        if (practicaJson == null || practicaJson.trim().isEmpty()) {
            tvResumenDatosExperimentales.setText("No hay práctica experimental registrada.");
            return;
        }

        try {
            JSONObject data = new JSONObject(practicaJson);

            StringBuilder resumen = new StringBuilder();

            if (data.has("observations")) {
                resumen.append("Observaciones:\n")
                        .append(data.optString("observations", ""))
                        .append("\n\n");
            }

            if (data.has("calculations")) {
                resumen.append("Cálculos realizados:\n")
                        .append(data.optString("calculations", ""))
                        .append("\n\n");
            }

            if (data.has("conclusions")) {
                resumen.append("Conclusiones:\n")
                        .append(data.optString("conclusions", ""))
                        .append("\n\n");
            }

            String evidenciasJson = sessionStore.getEvidenciasJson(asignacionId);
            if (evidenciasJson != null && !evidenciasJson.trim().isEmpty()) {
                JSONArray evidencias = new JSONArray(evidenciasJson);
                resumen.append("Evidencias agregadas: ")
                        .append(evidencias.length());
            }

            if (resumen.length() == 0) {
                resumen.append("La práctica experimental no tiene datos registrados.");
            }

            tvResumenDatosExperimentales.setText(resumen.toString().trim());

        } catch (Exception e) {
            e.printStackTrace();
            tvResumenDatosExperimentales.setText("No se pudo leer la práctica experimental registrada.");
        }
    }

    private void cargarComparacionGuardada() {
        String json = sessionStore.getComparacionResultadosJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            comparisonData = new JSONObject();

            String textoViejo = sessionStore.getComparacionTexto(asignacionId);
            if (textoViejo != null && !textoViejo.trim().isEmpty()) {
                try {
                    comparisonData.put("analysis", textoViejo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return;
        }

        try {
            comparisonData = new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
            comparisonData = new JSONObject();
        }
    }

    private void renderComparisonFields() {
        layoutComparisonFieldsContainer.removeAllViews();

        if (comparisonObject == null) {
            addTextInputField("analysis", "Análisis de la comparación", true);
            return;
        }

        JSONArray fields = comparisonObject.optJSONArray("fields");

        if (fields == null || fields.length() == 0) {
            addTextInputField("analysis", "Análisis de la comparación", true);
            return;
        }

        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.optJSONObject(i);
            if (field == null) continue;

            String id = field.optString("id", "");
            String label = field.optString("label", id);
            String type = field.optString("type", "");
            boolean required = field.optBoolean("required", false);

            if ("TEXT".equalsIgnoreCase(type)) {
                addTextInputField(id, label, required);
            }
        }
    }

    private void addTextInputField(String id, String label, boolean required) {
        TextView tvLabel = new TextView(this);
        tvLabel.setText(required ? label + " *" : label);
        tvLabel.setTextColor(android.graphics.Color.parseColor("#001B6B"));
        tvLabel.setTextSize(16);
        tvLabel.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, dpToPx(8), 0, dpToPx(8));
        tvLabel.setLayoutParams(labelParams);

        EditText editText = new EditText(this);
        editText.setTag(id);
        editText.setMinLines(5);
        editText.setGravity(Gravity.TOP);
        editText.setHint("Escribe " + label.toLowerCase(java.util.Locale.ROOT));
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setTextColor(android.graphics.Color.parseColor("#334155"));
        editText.setTextSize(14);
        editText.setBackgroundResource(R.drawable.edittext_redondo);
        editText.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));

        String savedValue = comparisonData.optString(id, "");
        editText.setText(savedValue);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(150)
        );
        editText.setLayoutParams(inputParams);

        layoutComparisonFieldsContainer.addView(tvLabel);
        layoutComparisonFieldsContainer.addView(editText);
    }

    /*
    private void guardarAnalisisYCompletar() {
        if (!validarComparisonFields()) {
            return;
        }

        guardarComparisonFields();

        Toast.makeText(this, "Comparación guardada", Toast.LENGTH_SHORT).show();

        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }
     */

    private void guardarAnalisisYCompletar() {
        if (modoSoloLectura()) {
            finish();
            return;
        }

        if (!validarComparisonFields()) {
            return;
        }

        guardarComparisonFields();

        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);

        StepCompletionOverlay.show(this, () -> {
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private boolean validarComparisonFields() {
        if (comparisonObject == null) {
            return validarCampoPorTag("analysis", "Análisis de la comparación", true);
        }

        JSONArray fields = comparisonObject.optJSONArray("fields");

        if (fields == null || fields.length() == 0) {
            return validarCampoPorTag("analysis", "Análisis de la comparación", true);
        }

        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.optJSONObject(i);
            if (field == null) continue;

            String id = field.optString("id", "");
            String label = field.optString("label", id);
            String type = field.optString("type", "");
            boolean required = field.optBoolean("required", false);

            if ("TEXT".equalsIgnoreCase(type)) {
                if (!validarCampoPorTag(id, label, required)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean validarCampoPorTag(String id, String label, boolean required) {
        if (!required) return true;

        EditText editText = layoutComparisonFieldsContainer.findViewWithTag(id);

        if (editText == null || editText.getText().toString().trim().isEmpty()) {
            if (editText != null) {
                editText.setError("Campo obligatorio");
                editText.requestFocus();
            }

            Toast.makeText(this, "Completa el campo: " + label, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void guardarComparisonFields() {
        try {
            JSONObject data = new JSONObject();

            recogerInputsDesdeContenedor(layoutComparisonFieldsContainer, data);

            data.put("left_source", comparisonObject != null
                    ? comparisonObject.optString("left_source", "experimental_practice")
                    : "experimental_practice");

            data.put("right_source", comparisonObject != null
                    ? comparisonObject.optString("right_source", "simulation_ar")
                    : "simulation_ar");

            data.put("updatedAt", obtenerFechaActual());

            sessionStore.saveComparacionResultadosJson(asignacionId, data.toString());

            String analysis = data.optString("analysis", "");
            if (!analysis.trim().isEmpty()) {
                sessionStore.saveComparacionTexto(asignacionId, analysis);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recogerInputsDesdeContenedor(View view, JSONObject data) throws Exception {
        if (view instanceof EditText) {
            Object tag = view.getTag();

            if (tag != null) {
                String id = tag.toString();
                String value = ((EditText) view).getText().toString().trim();
                data.put(id, value);
            }

            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                recogerInputsDesdeContenedor(group.getChildAt(i), data);
            }
        }
    }
    // Helpers
    private String traducirFuente(String source) {
        if (source == null) return "No definida";

        switch (source) {
            case "experimental_practice":
                return "Práctica experimental";
            case "simulation_ar":
                return "Simulación AR";
            default:
                return source;
        }
    }

    private String obtenerFechaActual() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                .format(new java.util.Date());
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String formatDecimal(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private String formatMetro(double value) {
        return String.format(java.util.Locale.US, "%.2f m", value);
    }

    private void setEditableRecursive(View view, boolean enabled) {
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            editText.setEnabled(enabled);
            editText.setFocusable(enabled);
            editText.setFocusableInTouchMode(enabled);
            editText.setCursorVisible(enabled);
            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                setEditableRecursive(group.getChildAt(i), enabled);
            }
        }
    }

    // Helper de estado
    private boolean pasoYaCompletado() {
        return asignacionId > 0
                && ordenPaso > 0
                && sessionStore.estaPasoCompletado(asignacionId, ordenPaso);
    }

    private boolean laboratorioYaEntregado() {
        return asignacionId > 0
                && sessionStore.isEntregaEnviada(asignacionId);
    }

    private boolean modoSoloLectura() {
        return pasoYaCompletado() || laboratorioYaEntregado();
    }

}