package com.marcos.fisikappmovil.ui.common;

import android.graphics.Color;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONObject;

public class KatexWebViewRenderer {

    private KatexWebViewRenderer() {
        // Utility class
    }

    public static void configure(WebView webView) {
        if (webView == null) return;

        webView.setBackgroundColor(Color.TRANSPARENT);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
    }

    public static void render(WebView webView, String latex) {
        if (webView == null) return;

        String safeLatex = latex == null || latex.trim().isEmpty()
                ? "\\text{Sin fórmula disponible}"
                : latex.trim();

        webView.loadDataWithBaseURL(
                "file:///android_asset/katex/",
                buildLatexHtml(safeLatex),
                "text/html",
                "UTF-8",
                null
        );
    }

    private static String buildLatexHtml(String latex) {
        String latexJsString = JSONObject.quote(latex == null ? "" : latex);

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<link rel='stylesheet' href='katex.min.css'>" +
                "<script src='katex.min.js'></script>" +
                "<style>" +
                "html, body {" +
                "  margin: 0;" +
                "  padding: 0;" +
                "  background: transparent;" +
                "  color: #0F172A;" +
                "  overflow: hidden;" +
                "}" +
                "body {" +
                "  display: flex;" +
                "  align-items: center;" +
                "  justify-content: center;" +
                "  min-height: 70px;" +
                "}" +
                "#formula {" +
                "  width: 100%;" +
                "  text-align: center;" +
                "  font-size: 20px;" +
                "  line-height: 1.3;" +
                "}" +
                ".katex-display {" +
                "  margin: 0;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div id='formula'></div>" +
                "<script>" +
                "try {" +
                "  const latex = " + latexJsString + ";" +
                "  katex.render(latex, document.getElementById('formula'), {" +
                "    throwOnError: false," +
                "    displayMode: true" +
                "  });" +
                "} catch (e) {" +
                "  document.getElementById('formula').innerText = " + latexJsString + ";" +
                "}" +
                "</script>" +
                "</body>" +
                "</html>";
    }
}