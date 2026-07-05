package com.marcos.fisikappmovil.ui.common;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.marcos.fisikappmovil.R;

public class StepCompletionOverlay {

    private static final long ANIMATION_DURATION_MS = 1700L;
    private static final long HOLD_AFTER_ANIMATION_MS = 450L;

    private StepCompletionOverlay() {
    }

    public static void show(Activity activity, Runnable onComplete) {
        if (activity == null || activity.isFinishing()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();

        FrameLayout overlay = new FrameLayout(activity);
        overlay.setClickable(true);
        overlay.setFocusable(true);

        // Fondo blanco semitransparente.
        overlay.setBackgroundColor(Color.argb(225, 255, 255, 255));

        // CLAVE: debe iniciar invisible.
        overlay.setAlpha(0f);

        FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        WebView webView = new WebView(activity);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                dpToPx(activity, 520),
                dpToPx(activity, 520)
        );
        webParams.gravity = Gravity.CENTER;

        overlay.addView(webView, webParams);
        decorView.addView(overlay, overlayParams);

        // FadeIn del overlay.
        overlay.post(() -> {
            overlay.animate()
                    .alpha(1f)
                    .setDuration(300L)
                    .start();
        });

        // Cargar el SVG un poco después para que primero se note la cortina.
        overlay.postDelayed(() -> {
            webView.loadDataWithBaseURL(
                    "file:///android_asset/animations/",
                    buildSvgHtml(),
                    "text/html",
                    "UTF-8",
                    null
            );
        }, 180L);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!activity.isFinishing()) {
                try {
                    decorView.removeView(overlay);
                } catch (Exception ignored) {
                }
            }

            if (onComplete != null) {
                onComplete.run();
            }

        }, ANIMATION_DURATION_MS + HOLD_AFTER_ANIMATION_MS + 180L);
    }

    private static int dpToPx(Activity activity, int dp) {
        float density = activity.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private static String buildSvgHtml() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "html, body {" +
                "  margin: 0;" +
                "  padding: 0;" +
                "  width: 100%;" +
                "  height: 100%;" +
                "  overflow: hidden;" +
                "  background: transparent;" +
                "}" +
                "body {" +
                "  display: flex;" +
                "  align-items: center;" +
                "  justify-content: center;" +
                "}" +
                "img {" +
                "  width: 100%;" +
                "  height: 100%;" +
                "  object-fit: contain;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<img src='success_step.svg' />" +
                "</body>" +
                "</html>";
    }
}