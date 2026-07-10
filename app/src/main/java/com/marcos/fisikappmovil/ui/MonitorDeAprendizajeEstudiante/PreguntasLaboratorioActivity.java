package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
import com.marcos.fisikappmovil.ui.common.KatexWebViewRenderer;
import com.marcos.fisikappmovil.ui.common.StepCompletionOverlay;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class PreguntasLaboratorioActivity extends AppCompatActivity {

    private TextView tvTituloPreguntas;
    private TextView tvDescripcionPreguntas;
    private LinearLayout layoutFormulasPreguntas;
    private LinearLayout layoutPreguntasContainer;
    private Button btnGuardar;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    private String stepId;
    private String stepTitle;

    private JSONObject questionsStep;
    private JSONArray questionsArray;

    private final Map<String, EditText> answerInputs = new LinkedHashMap<>();
    private final Map<String, JSONObject> questionByKey = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preguntas_laboratorio);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();

        cargarStepPreguntas();
        renderStepPreguntas();
        cargarRespuestasGuardadas();

        initListeners();
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);

        stepId = intent.getStringExtra("STEP_ID");
        stepTitle = intent.getStringExtra("STEP_TITLE");
    }

    private void initViews() {
        tvTituloPreguntas = findViewById(R.id.tvTituloPreguntas);
        tvDescripcionPreguntas = findViewById(R.id.tvDescripcionPreguntas);
        layoutFormulasPreguntas = findViewById(R.id.layoutFormulasPreguntas);
        layoutPreguntasContainer = findViewById(R.id.layoutPreguntasContainer);
        btnGuardar = findViewById(R.id.btnGuardarPreguntas);
    }

    private void initListeners() {
        btnGuardar.setOnClickListener(v -> guardarRespuestas());
    }

    private void cargarStepPreguntas() {
        String json = sessionStore.getMobileResourceJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            questionsStep = null;
            questionsArray = new JSONArray();
            return;
        }

        try {
            JSONObject root = new JSONObject(json);
            JSONArray steps = root.optJSONArray("steps");

            if (steps == null || steps.length() == 0) {
                questionsStep = null;
                questionsArray = new JSONArray();
                return;
            }

            questionsStep = findQuestionsStep(steps);

            if (questionsStep == null) {
                questionsArray = new JSONArray();
                return;
            }

            questionsArray = questionsStep.optJSONArray("questions");

            if (questionsArray == null) {
                questionsArray = new JSONArray();
            }

        } catch (Exception e) {
            e.printStackTrace();
            questionsStep = null;
            questionsArray = new JSONArray();
        }
    }

    private JSONObject findQuestionsStep(JSONArray steps) {
        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.optJSONObject(i);
            if (step == null) continue;

            String id = step.optString("id", "");
            String type = step.optString("type", "");
            int order = step.optInt("order", -1);

            if (stepId != null && stepId.equals(id)) {
                return step;
            }

            if (ordenPaso > 0 && order == ordenPaso) {
                return step;
            }

            if ("QUESTIONS".equalsIgnoreCase(type)) {
                return step;
            }
        }

        return null;
    }

    private void renderStepPreguntas() {
        answerInputs.clear();
        questionByKey.clear();

        layoutFormulasPreguntas.removeAllViews();
        layoutPreguntasContainer.removeAllViews();

        String title = stepTitle;

        if (!notEmpty(title) && questionsStep != null) {
            title = questionsStep.optString("title", "Preguntas de comprensión");
        }

        if (!notEmpty(title)) {
            title = "Preguntas de comprensión";
        }

        tvTituloPreguntas.setText(title);

        String description = "";

        if (questionsStep != null) {
            description = questionsStep.optString(
                    "description",
                    questionsStep.optString("prompt", "")
            );
        }

        if (notEmpty(description)) {
            tvDescripcionPreguntas.setVisibility(View.VISIBLE);
            tvDescripcionPreguntas.setText(description);
        } else {
            tvDescripcionPreguntas.setVisibility(View.VISIBLE);
            tvDescripcionPreguntas.setText("Responde las preguntas del laboratorio.");
        }

        renderFormulas();
        renderQuestions();
    }

    private void renderFormulas() {
        JSONArray formulas = null;

        if (questionsStep != null) {
            formulas = questionsStep.optJSONArray("formulas");
        }

        if (formulas == null || formulas.length() == 0) {
            formulas = buscarFormulasGlobalesDesdeMobileResource();
        }

        if (formulas == null || formulas.length() == 0) {
            layoutFormulasPreguntas.setVisibility(View.GONE);
            return;
        }

        layoutFormulasPreguntas.setVisibility(View.VISIBLE);

        TextView title = new TextView(this);
        title.setText("Fórmulas de apoyo");
        title.setTextColor(Color.parseColor("#001B6B"));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, dpToPx(8));
        layoutFormulasPreguntas.addView(title);

        for (int i = 0; i < formulas.length(); i++) {
            JSONObject formula = formulas.optJSONObject(i);
            if (formula == null) continue;

            String name = formula.optString("name", "Fórmula");
            String expression = formula.optString("expression", "");
            String formulaDescription = formula.optString("description", "");

            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextColor(Color.parseColor("#334155"));
            tvName.setTextSize(15);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setPadding(0, dpToPx(8), 0, dpToPx(4));
            layoutFormulasPreguntas.addView(tvName);

            WebView webView = new WebView(this);
            webView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(72)
            ));
            KatexWebViewRenderer.configure(webView);
            KatexWebViewRenderer.render(webView, expression);
            layoutFormulasPreguntas.addView(webView);

            if (notEmpty(formulaDescription)) {
                TextView tvDesc = new TextView(this);
                tvDesc.setText(formulaDescription);
                tvDesc.setTextColor(Color.parseColor("#64748B"));
                tvDesc.setTextSize(14);
                tvDesc.setPadding(0, 0, 0, dpToPx(8));
                layoutFormulasPreguntas.addView(tvDesc);
            }
        }
    }

    private JSONArray buscarFormulasGlobalesDesdeMobileResource() {
        String json = sessionStore.getMobileResourceJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            JSONObject root = new JSONObject(json);
            JSONArray steps = root.optJSONArray("steps");

            if (steps == null) return null;

            for (int i = 0; i < steps.length(); i++) {
                JSONObject step = steps.optJSONObject(i);
                if (step == null) continue;

                String type = step.optString("type", "");

                if (!"FORMULAS".equalsIgnoreCase(type)) {
                    continue;
                }

                JSONArray formulas = step.optJSONArray("formulas");

                if (formulas != null && formulas.length() > 0) {
                    return formulas;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void renderQuestions() {
        if (questionsArray == null || questionsArray.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText("Este paso no tiene preguntas configuradas.");
            empty.setTextColor(Color.parseColor("#64748B"));
            empty.setTextSize(15);
            layoutPreguntasContainer.addView(empty);
            btnGuardar.setEnabled(false);
            return;
        }

        btnGuardar.setEnabled(true);

        for (int i = 0; i < questionsArray.length(); i++) {
            JSONObject question = questionsArray.optJSONObject(i);
            if (question == null) continue;

            String questionKey = getQuestionKey(question, i);
            String title = question.optString(
                    "title",
                    question.optString("question", "Pregunta " + (i + 1))
            );
            String prompt = question.optString("prompt", "");
            String inputType = question.optString("input_type", "TEXTAREA");
            boolean required = question.optBoolean("required", true);

            questionByKey.put(questionKey, question);

            TextView tvQuestion = new TextView(this);
            tvQuestion.setText((i + 1) + ". " + title + (required ? " *" : ""));
            tvQuestion.setTextColor(Color.parseColor("#334155"));
            tvQuestion.setTextSize(15);
            tvQuestion.setTypeface(null, Typeface.BOLD);
            tvQuestion.setPadding(0, dpToPx(16), 0, dpToPx(6));
            layoutPreguntasContainer.addView(tvQuestion);

            if (notEmpty(prompt) && !prompt.equals(title)) {
                TextView tvPrompt = new TextView(this);
                tvPrompt.setText(prompt);
                tvPrompt.setTextColor(Color.parseColor("#64748B"));
                tvPrompt.setTextSize(14);
                tvPrompt.setPadding(0, 0, 0, dpToPx(8));
                layoutPreguntasContainer.addView(tvPrompt);
            }

            EditText input = buildInputForQuestion(inputType);
            input.setHint("Escribe tu respuesta...");
            layoutPreguntasContainer.addView(input);

            answerInputs.put(questionKey, input);
        }
    }

    private EditText buildInputForQuestion(String inputType) {
        EditText input = new EditText(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                "TEXT".equalsIgnoreCase(inputType) ? dpToPx(56) : dpToPx(120)
        );

        params.setMargins(0, 0, 0, dpToPx(6));
        input.setLayoutParams(params);

        input.setBackgroundResource(R.drawable.edittext_redondo);
        input.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
        input.setTextColor(Color.parseColor("#0F172A"));
        input.setHintTextColor(Color.parseColor("#94A3B8"));

        if ("NUMBER".equalsIgnoreCase(inputType)) {
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setGravity(android.view.Gravity.CENTER_VERTICAL);
        } else if ("DECIMAL".equalsIgnoreCase(inputType)) {
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setGravity(android.view.Gravity.CENTER_VERTICAL);
        } else if ("TEXT".equalsIgnoreCase(inputType)) {
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setSingleLine(false);
            input.setGravity(android.view.Gravity.CENTER_VERTICAL);
        } else {
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setGravity(android.view.Gravity.TOP);
            input.setMinLines(3);
        }

        return input;
    }

    private void cargarRespuestasGuardadas() {
        String jsonGuardado = sessionStore.getPreguntasJson(asignacionId);

        if (jsonGuardado == null || jsonGuardado.trim().isEmpty()) {
            return;
        }

        try {
            JSONArray array = new JSONArray(jsonGuardado);

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;

                String key = item.optString(
                        "question_key",
                        item.optString("key", item.optString("id", ""))
                );

                String respuesta = item.optString(
                        "answer",
                        item.optString("respuesta", "")
                );

                EditText input = answerInputs.get(key);

                if (input != null) {
                    input.setText(respuesta);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarRespuestas() {
        if (questionsArray == null || questionsArray.length() == 0) {
            return;
        }

        try {
            JSONArray answers = new JSONArray();

            for (Map.Entry<String, EditText> entry : answerInputs.entrySet()) {
                String questionKey = entry.getKey();
                EditText input = entry.getValue();
                JSONObject question = questionByKey.get(questionKey);

                if (question == null || input == null) continue;

                String respuesta = input.getText().toString().trim();
                boolean required = question.optBoolean("required", true);

                if (required && respuesta.isEmpty()) {
                    input.setError("Responde esta pregunta");
                    input.requestFocus();
                    return;
                }

                JSONObject item = new JSONObject();

                item.put("question_id", question.opt("id"));
                item.put("question_key", questionKey);
                item.put("question_type", question.optString("question_type", ""));
                item.put("title", question.optString("title", ""));
                item.put("prompt", question.optString("prompt", ""));
                item.put("input_type", question.optString("input_type", "TEXTAREA"));
                item.put("required", required);
                item.put("answer", respuesta);
                item.put("answered_at", obtenerFechaActualUtc());

                answers.put(item);
            }

            sessionStore.savePreguntasJson(asignacionId, answers.toString());

            completarPaso();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void completarPaso() {
        StepCompletionOverlay.show(this, () -> {
            Intent data = new Intent();
            data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private String getQuestionKey(JSONObject question, int index) {
        String key = question.optString("key", "");

        if (!notEmpty(key)) {
            key = question.optString("question_key", "");
        }

        if (!notEmpty(key)) {
            Object id = question.opt("id");

            if (id != null) {
                key = String.valueOf(id);
            }
        }

        if (!notEmpty(key)) {
            key = "question_" + index;
        }

        return key;
    }

    private String obtenerFechaActualUtc() {
        java.text.SimpleDateFormat format =
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);

        format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        return format.format(new java.util.Date());
    }

    private boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}