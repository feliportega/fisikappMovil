package com.marcos.fisikappmovil.ui.Laboratorio;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Typeface;
import android.graphics.Color;
import android.content.Intent;


import android.webkit.WebView;
import com.marcos.fisikappmovil.ui.common.KatexWebViewRenderer;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
import com.marcos.fisikappmovil.mapper.StepRenderMapper;
import com.marcos.fisikappmovil.model.RenderBlockItem;
import com.marcos.fisikappmovil.remote.response.MobileResourceResponse;
import com.marcos.fisikappmovil.remote.response.MobileStepResponse;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.PasosLaboratorio;
import com.marcos.fisikappmovil.ui.common.StepCompletionOverlay;

import java.util.List;

public class RenderContentActivity extends AppCompatActivity {

    public static final String EXTRA_ASSIGNMENT_ID = "assignment_id";
    public static final String EXTRA_STEP_ID = "step_id";
    public static final String EXTRA_STEP_TITLE = "step_title";

    private TextView btnBack;
    private TextView tvRenderTitle;
    private LinearLayout layoutRenderContent;
    private Button btnCompleteStep;

    private int assignmentId;
    private int ordenPaso = -1;
    private String stepId;
    private String stepTitle;

    private LaboratorioSessionStore sessionStore;
    private final Gson gson = new Gson();
    private final StepRenderMapper mapper = new StepRenderMapper();

    private MobileResourceResponse mobileResourceResponse;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render_content);

        readExtras();
        initViews();
        initListeners();

        sessionStore = new LaboratorioSessionStore(this);

        configurarBotonFinal();
        initListeners();

        tvRenderTitle.setText(stepTitle == null || stepTitle.trim().isEmpty() ? "Contenido" : stepTitle);

        cargarStepDesdeCache();
    }

    private void cargarStepDesdeCache() {
        if (assignmentId <= 0) {
            addEmptyView("No se recibió una asignación válida.");
            return;
        }

        if (stepId == null || stepId.trim().isEmpty()) {
            addEmptyView("No se recibió un paso válido.");
            return;
        }

        String json = sessionStore.getMobileResourceJson(assignmentId);

        if (json == null || json.trim().isEmpty()) {
            addEmptyView("No se encontró información guardada del laboratorio.");
            return;
        }

        try {
            mobileResourceResponse = gson.fromJson(json, MobileResourceResponse.class);

            MobileStepResponse step = findStepById(stepId);

            if (step == null) {
                addEmptyView("No se encontró el contenido de este paso.");
                return;
            }

            if (step.getTitle() != null && !step.getTitle().trim().isEmpty()) {
                tvRenderTitle.setText(step.getTitle());
            }

            renderStep(step);

        } catch (Exception e) {
            addEmptyView("No se pudo leer la información guardada del laboratorio.");
        }
    }

    private MobileStepResponse findStepById(String stepId) {
        if (mobileResourceResponse == null || mobileResourceResponse.getSteps() == null) {
            return null;
        }

        for (MobileStepResponse step : mobileResourceResponse.getSteps()) {
            if (step == null || step.getId() == null) continue;

            if (step.getId().equals(stepId)) {
                return step;
            }
        }

        return null;
    }

    private void readExtras() {
        Intent intent = getIntent();

        assignmentId = intent.getIntExtra(EXTRA_ASSIGNMENT_ID, -1);

        if (assignmentId == -1) {
            assignmentId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        }

        ordenPaso = intent.getIntExtra(
                PasosLaboratorio.EXTRA_ORDEN_PASO,
                -1
        );

        stepId = intent.getStringExtra(EXTRA_STEP_ID);
        stepTitle = intent.getStringExtra(EXTRA_STEP_TITLE);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvRenderTitle = findViewById(R.id.tvRenderTitle);
        layoutRenderContent = findViewById(R.id.layoutRenderContent);
        btnCompleteStep = findViewById(R.id.btnCompleteStep);
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCompleteStep.setOnClickListener(v -> {
            if (modoSoloLectura()) {
                finish();
                return;
            }

            Intent result = new Intent();
            result.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);

            StepCompletionOverlay.show(this, () -> {
                setResult(RESULT_OK, result);
                finish();
            });
        });
        /*
        btnCompleteStep.setOnClickListener(v -> {

            int ordenPaso = getIntent().getIntExtra(
                    PasosLaboratorio.EXTRA_ORDEN_PASO,
                    -1
            );

            Intent result = new Intent();
            result.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);

            StepCompletionOverlay.show(this, () -> {
                setResult(RESULT_OK, result);
                finish();
            });
        });
         */
    }

    private void renderStep(MobileStepResponse step) {
        layoutRenderContent.removeAllViews();

        if (step == null) {
            addEmptyView("No se encontró el contenido de este paso.");
            return;
        }

        List<RenderBlockItem> blocks = mapper.map(step);

        for (RenderBlockItem block : blocks) {
            addBlockView(block);
        }
    }

    private void addBlockView(RenderBlockItem block) {
        if (block == null) return;

        switch (block.getType()) {
            case RenderBlockItem.TYPE_TITLE:
                addTitle(block.getValue());
                break;

            case RenderBlockItem.TYPE_TEXT:
                addText(block.getValue());
                break;

            case RenderBlockItem.TYPE_LIST:
                addList(block.getTitle(), block.getItems());
                break;

            case RenderBlockItem.TYPE_FORMULA:
                addFormula(block.getTitle(), block.getValue(), block.getDescription());
                break;

            case RenderBlockItem.TYPE_CARD:
                addCard(block.getTitle(), block.getValue());
                break;

            case RenderBlockItem.TYPE_NUMBERED_LIST:
                addNumberedList(block.getTitle(), block.getItems());
                break;

            default:
                addCard(block.getTitle(), block.getValue());
                break;
        }
    }

    private void addNumberedList(String title, List<String> items) {
        addCard(title, buildNumberedList(items));
    }

    private String buildNumberedList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "Sin elementos disponibles.";
        }

        StringBuilder builder = new StringBuilder();

        int index = 1;
        for (String item : items) {
            if (item == null || item.trim().isEmpty()) continue;

            String clean = item.trim();

            // Evita duplicar "1. 1. texto" si ya viene numerado.
            if (clean.matches("^\\d+\\.\\s+.*")) {
                builder.append(clean);
            } else {
                builder.append(index).append(". ").append(clean);
            }

            builder.append("\n");
            index++;
        }

        return builder.toString().trim();
    }

    private void addTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text == null ? "" : text);
        view.setTextColor(Color.parseColor("#001B6B"));
        view.setTextSize(20);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setPadding(0, 10, 0, 12);
        layoutRenderContent.addView(view);
    }

    private void addText(String text) {
        TextView view = new TextView(this);
        view.setText(text == null ? "" : text);
        view.setTextColor(Color.parseColor("#334155"));
        view.setTextSize(15);
        view.setLineSpacing(4f, 1.1f);
        view.setPadding(0, 8, 0, 14);
        layoutRenderContent.addView(view);
    }

    private void addList(String title, List<String> items) {
        addCard(title, buildBulletList(items));
    }

    private String buildBulletList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "Sin elementos disponibles.";
        }

        StringBuilder builder = new StringBuilder();

        for (String item : items) {
            if (item == null || item.trim().isEmpty()) continue;
            builder.append("• ").append(item.trim()).append("\n");
        }

        return builder.toString().trim();
    }

    private void addFormula(String title, String value, String description) {
        LinearLayout card = createCardContainer();

        if (title != null && !title.trim().isEmpty()) {
            card.addView(createCardTitle(title));
        }

        WebView webView = new WebView(this);

        LinearLayout.LayoutParams webParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(85)
        );
        webParams.setMargins(0, dpToPx(2), 0, dpToPx(2));
        webView.setLayoutParams(webParams);

        KatexWebViewRenderer.configure(webView);
        KatexWebViewRenderer.render(webView, value);

        card.addView(webView);

        if (description != null && !description.trim().isEmpty()) {
            TextView tvDescription = createCardValue(description);
            tvDescription.setPadding(0, dpToPx(2), 0, 0);
            card.addView(tvDescription);
        }

        layoutRenderContent.addView(card);
    }

    private void addCard(String title, String value) {
        LinearLayout card = createCardContainer();

        if (title != null && !title.trim().isEmpty()) {
            card.addView(createCardTitle(title));
        }

        card.addView(createCardValue(value));

        layoutRenderContent.addView(card);
    }

    private LinearLayout createCardContainer() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackgroundResource(R.drawable.bg_card_rounded);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 14);
        card.setLayoutParams(params);

        return card;
    }

    private TextView createCardTitle(String title) {
        TextView tvTitle = new TextView(this);
        tvTitle.setText(title == null ? "" : title);
        tvTitle.setTextColor(Color.parseColor("#001B6B"));
        tvTitle.setTextSize(16);
        tvTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tvTitle.setPadding(0, 0, 0, 8);
        return tvTitle;
    }

    private TextView createCardValue(String value) {
        TextView tvValue = new TextView(this);
        tvValue.setText(value == null || value.trim().isEmpty() ? "Sin información disponible." : value);
        tvValue.setTextColor(Color.parseColor("#475569"));
        tvValue.setTextSize(14);
        tvValue.setLineSpacing(4f, 1.1f);
        return tvValue;
    }

    private void addEmptyView(String message) {
        TextView view = new TextView(this);
        view.setText(message);
        view.setTextColor(Color.parseColor("#64748B"));
        view.setTextSize(15);
        view.setPadding(0, 30, 0, 0);
        layoutRenderContent.addView(view);
    }

    // Helpers
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private boolean pasoYaCompletado() {
        return assignmentId > 0
                && ordenPaso > 0
                && sessionStore.estaPasoCompletado(assignmentId, ordenPaso);
    }

    private boolean laboratorioYaEntregado() {
        return assignmentId > 0
                && sessionStore.isEntregaEnviada(assignmentId);
    }

    private boolean modoSoloLectura() {
        return pasoYaCompletado() || laboratorioYaEntregado();
    }

    private void configurarBotonFinal() {
        if (btnCompleteStep == null || sessionStore == null) {
            return;
        }

        if (modoSoloLectura()) {
            btnCompleteStep.setText("Regresar");
        } else {
            btnCompleteStep.setText("Completar");
        }
    }
}