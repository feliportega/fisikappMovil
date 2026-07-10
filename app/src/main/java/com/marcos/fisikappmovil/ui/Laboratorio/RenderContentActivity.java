package com.marcos.fisikappmovil.ui.Laboratorio;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Typeface;
import android.graphics.Color;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import android.widget.Toast;

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


import com.marcos.fisikappmovil.remote.response.MobileConceptResponse;
import com.marcos.fisikappmovil.remote.response.MobileResourceLinkResponse;

import java.util.List;

public class RenderContentActivity extends AppCompatActivity {

    public static final String EXTRA_ASSIGNMENT_ID = "assignment_id";
    public static final String EXTRA_STEP_ID = "step_id";
    public static final String EXTRA_STEP_TITLE = "step_title";

    private static final String APP_WEB_ORIGIN = "https://backend-fisikapp.onrender.com";

    private ImageView btnBack;
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

    private View fullscreenView;
    private WebChromeClient.CustomViewCallback fullscreenCallback;


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

        getOnBackPressedDispatcher().addCallback(
                this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (closeFullscreenIfNeeded()) {
                            return;
                        }

                        finish();
                    }
                }
        );

        tvRenderTitle.setText(stepTitle == null || stepTitle.trim().isEmpty() ? "Contenido" : stepTitle);

        cargarStepDesdeCache();
    }

    @Override
    protected void onDestroy() {
        closeFullscreenIfNeeded();
        super.onDestroy();
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
        btnBack.setOnClickListener(v -> {
            if (closeFullscreenIfNeeded()) {
                return;
            }

            finish();
        });

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

        if ("CONCEPTS".equalsIgnoreCase(step.getType())) {
            renderConceptResources(step);
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

    private void renderConceptResources(MobileStepResponse step) {
        if (step == null || step.getConcepts() == null || step.getConcepts().isEmpty()) {
            return;
        }

        for (MobileConceptResponse concept : step.getConcepts()) {
            if (concept == null || concept.getResources() == null || concept.getResources().isEmpty()) {
                continue;
            }

            for (MobileResourceLinkResponse resource : concept.getResources()) {
                if (resource == null) continue;

                String url = resource.getUrl();

                if (!isYoutubeUrl(url)) {
                    continue;
                }

                String embedUrl = buildYoutubeEmbedUrl(url);

                if (embedUrl == null || embedUrl.trim().isEmpty()) {
                    continue;
                }

                renderYoutubeCard(
                        resource.getName(),
                        embedUrl
                );
            }
        }
    }

    private void renderYoutubeCard(String title, String embedUrl) {
        LinearLayout card = createCardContainer();

        TextView tvTitle = createCardTitle(
                title == null || title.trim().isEmpty()
                        ? "Video de apoyo"
                        : title
        );

        card.addView(tvTitle);

        WebView webView = new WebView(this);

        LinearLayout.LayoutParams webParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(215)
        );

        webParams.setMargins(0, dpToPx(6), 0, dpToPx(8));
        webView.setLayoutParams(webParams);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(buildYoutubeWebChromeClient());

        // Inicia oculto.
        webView.setVisibility(View.GONE);

        TextView tvToggle = new TextView(this);
        tvToggle.setText("Mostrar video ▼");
        tvToggle.setTextColor(Color.parseColor("#001B6B"));
        tvToggle.setTextSize(15);
        tvToggle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tvToggle.setGravity(android.view.Gravity.CENTER);
        tvToggle.setPadding(0, dpToPx(12), 0, dpToPx(4));
        tvToggle.setBackgroundColor(Color.TRANSPARENT);

        final boolean[] videoVisible = {false};
        final boolean[] videoLoaded = {false};

        tvToggle.setOnClickListener(v -> {
            if (!videoVisible[0]) {
                webView.setVisibility(View.VISIBLE);

                if (!videoLoaded[0]) {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Referer", APP_WEB_ORIGIN + "/");
                    headers.put("Origin", APP_WEB_ORIGIN);

                    webView.loadUrl(embedUrl, headers);
                    videoLoaded[0] = true;
                }

                tvToggle.setText("Ocultar video ▲");
                videoVisible[0] = true;

            } else {
                if (closeFullscreenIfNeeded()) {
                    // Si estaba en fullscreen, primero lo cerramos.
                }

                webView.setVisibility(View.GONE);

                // Esto detiene el audio/video si estaba reproduciendo.
                webView.loadUrl("about:blank");
                videoLoaded[0] = false;

                tvToggle.setText("Mostrar video ▼");
                videoVisible[0] = false;
            }
        });

        card.addView(webView);
        card.addView(tvToggle);

        layoutRenderContent.addView(card);
    }
    /*
    private void renderYoutubeCard(String title, String embedUrl) {
        LinearLayout card = createCardContainer();

        TextView tvTitle = createCardTitle(
                title == null || title.trim().isEmpty()
                        ? "Video de apoyo"
                        : title
        );

        card.addView(tvTitle);

        WebView webView = new WebView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(215)
        );

        params.setMargins(0, dpToPx(6), 0, dpToPx(8));
        webView.setLayoutParams(params);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(buildYoutubeWebChromeClient());

        java.util.Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Referer", APP_WEB_ORIGIN + "/");
        headers.put("Origin", APP_WEB_ORIGIN);

        webView.loadUrl(embedUrl, headers);

        card.addView(webView);

        Button btnOpenYoutube = new Button(this);
        btnOpenYoutube.setText("Abrir video en YouTube");
        btnOpenYoutube.setAllCaps(false);
        btnOpenYoutube.setOnClickListener(v -> abrirUrlExterna(embedUrl));
        card.addView(btnOpenYoutube);

        layoutRenderContent.addView(card);
    }
*/
    private WebChromeClient buildYoutubeWebChromeClient() {
        return new WebChromeClient() {
            @Override
            public void onShowCustomView(
                    View view,
                    CustomViewCallback callback
            ) {
                if (fullscreenView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                fullscreenView = view;
                fullscreenCallback = callback;

                FrameLayout decorView = (FrameLayout) getWindow().getDecorView();

                decorView.addView(
                        fullscreenView,
                        new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                        )
                );

                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
            }

            @Override
            public void onHideCustomView() {
                closeFullscreenIfNeeded();
            }
        };
    }

    private boolean closeFullscreenIfNeeded() {
        if (fullscreenView == null) {
            return false;
        }

        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();

        decorView.removeView(fullscreenView);
        fullscreenView = null;

        if (fullscreenCallback != null) {
            fullscreenCallback.onCustomViewHidden();
            fullscreenCallback = null;
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        return true;
    }

    //helpers
    private boolean isYoutubeUrl(String url) {
        if (url == null) return false;

        String lower = url.toLowerCase();

        return lower.contains("youtube.com/watch")
                || lower.contains("youtu.be/")
                || lower.contains("youtube.com/embed/")
                || lower.contains("youtube.com/shorts/");
    }

    private String buildYoutubeEmbedUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        try {
            Uri uri = Uri.parse(url);

            String host = uri.getHost();
            String videoId = null;

            if (host == null) {
                return null;
            }

            host = host.toLowerCase();

            if (host.contains("youtube.com")) {
                String path = uri.getPath();

                if (path != null && path.startsWith("/watch")) {
                    videoId = uri.getQueryParameter("v");
                } else if (path != null && path.startsWith("/embed/")) {
                    videoId = path.replace("/embed/", "");
                } else if (path != null && path.startsWith("/shorts/")) {
                    videoId = path.replace("/shorts/", "");
                }
            } else if (host.contains("youtu.be")) {
                String path = uri.getPath();

                if (path != null && path.length() > 1) {
                    videoId = path.substring(1);
                }
            }

            if (videoId == null || videoId.trim().isEmpty()) {
                return null;
            }

            videoId = videoId.trim();

            int queryIndex = videoId.indexOf("?");
            if (queryIndex != -1) {
                videoId = videoId.substring(0, queryIndex);
            }

            int slashIndex = videoId.indexOf("/");
            if (slashIndex != -1) {
                videoId = videoId.substring(0, slashIndex);
            }

            int start = extractYoutubeStartSeconds(uri);

            StringBuilder embedUrl = new StringBuilder();

            embedUrl.append("https://www.youtube-nocookie.com/embed/")
                    .append(videoId)
                    .append("?rel=0")
                    .append("&modestbranding=1")
                    .append("&playsinline=1")
                    .append("&enablejsapi=1")
                    .append("&origin=")
                    .append(Uri.encode(APP_WEB_ORIGIN));

            if (start > 0) {
                embedUrl.append("&start=").append(start);
            }

            return embedUrl.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int extractYoutubeStartSeconds(Uri uri) {
        if (uri == null) return 0;

        String t = uri.getQueryParameter("t");

        if (t == null || t.trim().isEmpty()) {
            return 0;
        }

        try {
            t = t.trim().toLowerCase();

            int total = 0;

            if (t.contains("h")) {
                String[] parts = t.split("h", 2);
                total += Integer.parseInt(parts[0]) * 3600;
                t = parts.length > 1 ? parts[1] : "";
            }

            if (t.contains("m")) {
                String[] parts = t.split("m", 2);
                total += Integer.parseInt(parts[0]) * 60;
                t = parts.length > 1 ? parts[1] : "";
            }

            if (t.endsWith("s")) {
                t = t.substring(0, t.length() - 1);
            }

            if (!t.trim().isEmpty()) {
                total += Integer.parseInt(t.trim());
            }

            return total;

        } catch (Exception e) {
            return 0;
        }
    }

    private void abrirUrlExterna(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el video.", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildYoutubeHtml(String embedUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "html, body {" +
                "margin:0;" +
                "padding:0;" +
                "background:#000;" +
                "height:100%;" +
                "width:100%;" +
                "overflow:hidden;" +
                "}" +
                ".video-container {" +
                "position:relative;" +
                "width:100%;" +
                "height:100%;" +
                "background:#000;" +
                "}" +
                "iframe {" +
                "position:absolute;" +
                "top:0;" +
                "left:0;" +
                "width:100%;" +
                "height:100%;" +
                "border:0;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='video-container'>" +
                "<iframe " +
                "width='560' " +
                "height='315' " +
                "src='" + embedUrl + "' " +
                "title='YouTube video player' " +
                "frameborder='0' " +
                "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share' " +
                "referrerpolicy='strict-origin-when-cross-origin' " +
                "allowfullscreen>" +
                "</iframe>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}