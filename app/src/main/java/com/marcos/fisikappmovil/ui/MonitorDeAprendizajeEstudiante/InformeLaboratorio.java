package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
import com.marcos.fisikappmovil.ui.common.StepCompletionOverlay;

import org.json.JSONArray;
import org.json.JSONObject;

public class InformeLaboratorio extends AppCompatActivity {

    // CORREGIDO: Se cambia el nombre de la variable para que coincida con el XML unificado
    private ImageView btnBackPasosLab;

    private TextView tvNombreLaboratorioInforme;
    private TextView tvInfoGeneralInforme;
    private TextView tvResumenArInforme;
    private TextView tvIntentosArInforme;
    private TextView tvPreguntasInforme;
    private TextView tvDatosExperimentalesInforme;
    private TextView tvComparacionInforme;

    private Button btnGuardarInforme;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    private TextView tvReportInstructions;
    private LinearLayout layoutReportSectionsContainer;

    private CardView cardDatosExperimentalesInforme;
    private CardView cardComparacionInforme;

    private JSONObject reportObject;
    private JSONObject reportData = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_laboratorio);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        initListeners();

        cargarReportDesdeJson();
        cargarInformeGuardado();

        pintarInforme();
        renderReportSections();
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
        // CORREGIDO: Enlace modificado al ID correcto del XML
        btnBackPasosLab = findViewById(R.id.btnBackDetalleLab);

        tvNombreLaboratorioInforme = findViewById(R.id.tvNombreLaboratorioInforme);
        tvInfoGeneralInforme = findViewById(R.id.tvInfoGeneralInforme);
        tvResumenArInforme = findViewById(R.id.tvResumenArInforme);
        tvIntentosArInforme = findViewById(R.id.tvIntentosArInforme);
        tvPreguntasInforme = findViewById(R.id.tvPreguntasInforme);
        tvDatosExperimentalesInforme = findViewById(R.id.tvDatosExperimentalesInforme);
        tvComparacionInforme = findViewById(R.id.tvComparacionInforme);

        tvReportInstructions = findViewById(R.id.tvReportInstructions);
        layoutReportSectionsContainer = findViewById(R.id.layoutReportSectionsContainer);

        cardDatosExperimentalesInforme = findViewById(R.id.cardDatosExperimentalesInforme);
        cardComparacionInforme = findViewById(R.id.cardComparacionInforme);

        btnGuardarInforme = findViewById(R.id.btnGuardarInforme);
    }

    private void initListeners() {
        // CORREGIDO: Listener apuntando a la variable actualizada
        btnBackPasosLab.setOnClickListener(v -> finish());
        btnGuardarInforme.setOnClickListener(v -> guardarInforme());
    }

    private void cargarInformeGuardado() {
        String json = sessionStore.getInformeLaboratorioJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            reportData = new JSONObject();

            String conclusionesViejas = sessionStore.getConclusionesTexto(asignacionId);
            if (conclusionesViejas != null && !conclusionesViejas.trim().isEmpty()) {
                try {
                    reportData.put("conclusions", conclusionesViejas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return;
        }

        try {
            reportData = new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
            reportData = new JSONObject();
        }
    }

    private void guardarInforme() {
        if (modoSoloLectura()) {
            finish();
            return;
        }

        if (!validarReportSections()) {
            return;
        }

        guardarReportSections();

        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);

        StepCompletionOverlay.show(this, () -> {
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void guardarReportSections() {
        try {
            JSONObject data = new JSONObject();

            recogerInputsDesdeContenedor(layoutReportSectionsContainer, data);

            data.put("include_practice", reportObject == null
                    || reportObject.optBoolean("include_practice", true));

            data.put("include_simulation", reportObject == null
                    || reportObject.optBoolean("include_simulation", true));

            data.put("include_comparison", reportObject == null
                    || reportObject.optBoolean("include_comparison", true));

            data.put("updatedAt", obtenerFechaActual());

            sessionStore.saveInformeLaboratorioJson(asignacionId, data.toString());

            String conclusions = data.optString("conclusions", "");
            if (!conclusions.trim().isEmpty()) {
                sessionStore.saveConclusionesTexto(asignacionId, conclusions);
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

    private void pintarInforme() {
        if (tvNombreLaboratorioInforme.getText() == null
                || tvNombreLaboratorioInforme.getText().toString().trim().isEmpty()
                || "Laboratorio".equals(tvNombreLaboratorioInforme.getText().toString().trim())) {

            tvNombreLaboratorioInforme.setText("Laboratorio");
            tvInfoGeneralInforme.setText("Grupo " + grupoId + " · Asignación " + asignacionId);
        }

        pintarResumenAr();
        pintarPreguntas();
        pintarDatosPracticaExperimental();
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

    private void pintarDatosExperimental() {
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

    private void pintarDatosPracticaExperimental() {
        boolean includePractice = reportObject == null
                || reportObject.optBoolean("include_practice", true);

        if (!includePractice) {
            cardDatosExperimentalesInforme.setVisibility(View.GONE);
            return;
        }

        cardDatosExperimentalesInforme.setVisibility(View.VISIBLE);

        String practicaJson = sessionStore.getPracticaExperimentalJson(asignacionId);

        if (practicaJson == null || practicaJson.trim().isEmpty()) {
            tvDatosExperimentalesInforme.setText("No hay práctica experimental registrada.");
            return;
        }

        try {
            JSONObject data = new JSONObject(practicaJson);

            StringBuilder builder = new StringBuilder();

            if (data.has("observations")) {
                builder.append("Observaciones:\n")
                        .append(data.optString("observations", ""))
                        .append("\n\n");
            }

            if (data.has("calculations")) {
                builder.append("Cálculos realizados:\n")
                        .append(data.optString("calculations", ""))
                        .append("\n\n");
            }

            if (data.has("conclusions")) {
                builder.append("Conclusiones:\n")
                        .append(data.optString("conclusions", ""))
                        .append("\n\n");
            }

            String evidenciasJson = sessionStore.getEvidenciasJson(asignacionId);
            if (evidenciasJson != null && !evidenciasJson.trim().isEmpty()) {
                JSONArray evidencias = new JSONArray(evidenciasJson);
                builder.append("Evidencias agregadas: ")
                        .append(evidencias.length());
            }

            if (builder.length() == 0) {
                builder.append("La práctica experimental no tiene datos registrados.");
            }

            tvDatosExperimentalesInforme.setText(builder.toString().trim());

        } catch (Exception e) {
            e.printStackTrace();
            tvDatosExperimentalesInforme.setText("No se pudo leer la práctica experimental.");
        }
    }

    private void pintarComparacion() {
        boolean includeComparison = reportObject == null
                || reportObject.optBoolean("include_comparison", true);

        if (!includeComparison) {
            cardComparacionInforme.setVisibility(View.GONE);
            return;
        }

        cardComparacionInforme.setVisibility(View.VISIBLE);

        String comparacionJson = sessionStore.getComparacionResultadosJson(asignacionId);

        if (comparacionJson != null && !comparacionJson.trim().isEmpty()) {
            try {
                JSONObject data = new JSONObject(comparacionJson);

                StringBuilder builder = new StringBuilder();

                JSONArray names = data.names();

                if (names != null) {
                    for (int i = 0; i < names.length(); i++) {
                        String key = names.optString(i, "");

                        if ("left_source".equals(key)
                                || "right_source".equals(key)
                                || "updatedAt".equals(key)) {
                            continue;
                        }

                        String value = data.optString(key, "");

                        if (value == null || value.trim().isEmpty()) {
                            continue;
                        }

                        builder.append(formatFieldTitle(key))
                                .append(":\n")
                                .append(value)
                                .append("\n\n");
                    }
                }

                if (builder.length() == 0) {
                    builder.append("No hay análisis comparativo guardado.");
                }

                tvComparacionInforme.setText(builder.toString().trim());
                return;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String comparacionVieja = sessionStore.getComparacionTexto(asignacionId);

        if (comparacionVieja == null || comparacionVieja.trim().isEmpty()) {
            tvComparacionInforme.setText("No hay análisis comparativo guardado.");
            return;
        }

        tvComparacionInforme.setText(comparacionVieja);
    }

    private void cargarReportDesdeJson() {
        String json = sessionStore.getMobileResourceJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            tvReportInstructions.setText("No se encontró la configuración del informe.");
            return;
        }

        try {
            JSONObject root = new JSONObject(json);

            JSONObject resource = root.optJSONObject("resource");
            if (resource != null) {
                String title = resource.optString("title", "Laboratorio");
                String category = resource.optString("category", "Sin categoría");
                String teacher = resource.optString("teacher", "Sin docente");

                tvNombreLaboratorioInforme.setText(title);
                tvInfoGeneralInforme.setText(
                        category + " · " +
                                "Docente: " + teacher + " · " +
                                "Grupo " + grupoId + " · " +
                                "Asignación " + asignacionId
                );
            }

            JSONArray steps = root.optJSONArray("steps");

            if (steps == null || steps.length() == 0) {
                tvReportInstructions.setText("Este laboratorio no tiene pasos configurados.");
                return;
            }

            JSONObject reportStep = findReportStep(steps);

            if (reportStep == null) {
                tvReportInstructions.setText("No se encontró el paso de informe.");
                return;
            }

            reportObject = reportStep.optJSONObject("report");

            if (reportObject == null) {
                tvReportInstructions.setText("El paso de informe no tiene configuración.");
                return;
            }

            String instructions = reportObject.optString(
                    "instructions",
                    "Complete las secciones del informe de laboratorio."
            );

            tvReportInstructions.setText(instructions);

        } catch (Exception e) {
            e.printStackTrace();
            tvReportInstructions.setText("No se pudo leer la configuración del informe.");
        }
    }

    private JSONObject findReportStep(JSONArray steps) {
        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.optJSONObject(i);
            if (step == null) continue;

            String type = step.optString("type", "");
            int order = step.optInt("order", -1);

            if ("REPORT".equalsIgnoreCase(type)) {
                return step;
            }

            if (ordenPaso > 0 && order == ordenPaso) {
                return step;
            }
        }

        return null;
    }

    private void renderReportSections() {
        layoutReportSectionsContainer.removeAllViews();

        if (reportObject == null) {
            addReportTextInputField("conclusions", "Conclusiones", true);
            return;
        }

        JSONArray sections = reportObject.optJSONArray("sections");

        if (sections == null || sections.length() == 0) {
            addReportTextInputField("results", "Resultados obtenidos", true);
            addReportTextInputField("analysis", "Análisis", true);
            addReportTextInputField("conclusions", "Conclusiones", true);
            return;
        }

        for (int i = 0; i < sections.length(); i++) {
            JSONObject section = sections.optJSONObject(i);
            if (section == null) continue;

            String id = section.optString("id", "");
            String label = section.optString("label", id);
            String type = section.optString("type", "");
            boolean required = section.optBoolean("required", false);

            if ("TEXT".equalsIgnoreCase(type)) {
                addReportTextInputField(id, label, required);
            }
        }
    }

    private void addReportTextInputField(String id, String label, boolean required) {
        TextView tvLabel = new TextView(this);
        tvLabel.setText(required ? label + " *" : label);
        tvLabel.setTextColor(Color.parseColor("#001B6B"));
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
        editText.setTextColor(Color.parseColor("#334155"));
        editText.setTextSize(14);
        editText.setBackgroundResource(R.drawable.edittext_redondo);
        editText.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));

        String savedValue = reportData.optString(id, "");
        editText.setText(savedValue);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(150)
        );
        inputParams.setMargins(0, 0, 0, dpToPx(10));
        editText.setLayoutParams(inputParams);

        layoutReportSectionsContainer.addView(tvLabel);
        layoutReportSectionsContainer.addView(editText);
    }

    private boolean validarReportSections() {
        if (reportObject == null) {
            return validarCampoPorTag("conclusions", "Conclusiones", true);
        }

        JSONArray sections = reportObject.optJSONArray("sections");

        if (sections == null || sections.length() == 0) {
            return validarCampoPorTag("results", "Resultados obtenidos", true)
                    && validarCampoPorTag("analysis", "Análisis", true)
                    && validarCampoPorTag("conclusions", "Conclusiones", true);
        }

        for (int i = 0; i < sections.length(); i++) {
            JSONObject section = sections.optJSONObject(i);
            if (section == null) continue;

            String id = section.optString("id", "");
            String label = section.optString("label", id);
            String type = section.optString("type", "");
            boolean required = section.optBoolean("required", false);

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

        EditText editText = layoutReportSectionsContainer.findViewWithTag(id);

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

    // Helpers

    private String obtenerFechaActual() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                .format(new java.util.Date());
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String formatFieldTitle(String key) {
        if (key == null || key.trim().isEmpty()) {
            return "Campo";
        }

        String clean = key.replace("_", " ").trim();

        return clean.substring(0, 1).toUpperCase(java.util.Locale.ROOT)
                + clean.substring(1);
    }

    private String formatMetro(double value) {
        return String.format(java.util.Locale.US, "%.2f m", value);
    }

    private String formatDecimal(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private void configurarModoPantalla() {
        if (modoSoloLectura()) {
            btnGuardarInforme.setText("Regresar");
            setEditableRecursive(layoutReportSectionsContainer, false);
        } else {
            btnGuardarInforme.setText("Guardar informe");
            setEditableRecursive(layoutReportSectionsContainer, true);
        }
    }

    // Helpers
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

    // Helpers de estado
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