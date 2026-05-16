package com.marcos.fisikappmovil.ui.faceNet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;

import androidx.annotation.Nullable;

public class FaceGuideOverlayView extends View {

    private final Paint guidePaint = new Paint();
    private final Paint facePaint = new Paint();
    private final Paint textPaint = new Paint();

    private RectF faceBox;
    private Size frameSize = new Size(0, 0);
    private Size previewSize = new Size(0, 0);

    private long scanStartTime = System.currentTimeMillis();
    private boolean scanEnabled = true;
    private boolean showDebugBoxes = false; //mostrar box

    // Barra de scanner y spinner opacidad
    private float scanAlpha = 1f;
    private long spinnerStartTime = System.currentTimeMillis();
    private long lastAnimTime = System.currentTimeMillis();

    private int stableCounter = 0;
    private int requiredStableFrames = 60;

    private float guideOffsetYRatio = 0f;
    private float minSizeRatio = 0.22f; // tamanio minimo del rostro
    private float maxSizeRatio = 0.80f; // tamanio max
    private float tolerancePx = 40f;

    private boolean alignedNow = false;

    public enum OverlayState {
        SEARCHING,
        ALIGNING,
        VERIFYING,
        SUCCESS,
        ERROR
    }

    private OverlayState overlayState = OverlayState.SEARCHING;

    public void setOverlayState(OverlayState state) {
        this.overlayState = state;
        invalidate();
    }

    public FaceGuideOverlayView(Context context) {
        super(context);
        init();
    }

    public FaceGuideOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceGuideOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        guidePaint.setStyle(Paint.Style.STROKE);
        guidePaint.setStrokeWidth(6f);
        guidePaint.setAntiAlias(true);

        facePaint.setStyle(Paint.Style.STROKE);
        facePaint.setStrokeWidth(3f);
        facePaint.setColor(Color.YELLOW);
        facePaint.setAntiAlias(true);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(42f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
    }

    public void updateData(
            RectF faceBox,
            Size frameSize,
            Size previewSize,
            int stableCounter,
            int requiredStableFrames
    ) {
        this.faceBox = faceBox != null ? new RectF(faceBox) : null;
        this.frameSize = frameSize;
        this.previewSize = previewSize;
        this.stableCounter = stableCounter;
        this.requiredStableFrames = requiredStableFrames;
        invalidate();
    }

    private void updateScanAlpha() {
        long now = System.currentTimeMillis();
        float dt = Math.min(32f, now - lastAnimTime) / 1000f;
        lastAnimTime = now;

        float target = alignedNow ? 0f : 1f;
        float speed = 10.0f;

        scanAlpha += (target - scanAlpha) * speed * dt;

        if (scanAlpha < 0.01f) scanAlpha = 0f;
        if (scanAlpha > 0.99f) scanAlpha = 1f;
    }



    public boolean isAlignedNow() {
        return alignedNow;
    }
    // New canvas
    private RectF getMainGuideRect(float w, float h) {
        float frameW = w * 0.58f;
        float frameH = frameW * 1.35f;

        float left = (w - frameW) / 2f;
        float top = h * 0.22f;

        return new RectF(left, top, left + frameW, top + frameH);
    }

    // Spinner
    private void drawSegmentSpinner(Canvas canvas, RectF guideRect, float alphaFactor) {
        if (alphaFactor <= 0.01f) return;

        float w = getWidth();
        float h = getHeight();

        float cx = w / 2f;
        float cy = guideRect.bottom + h * 0.075f;

        float outerR = w * 0.075f;
        float innerR = outerR * 0.62f;

        long now = System.currentTimeMillis();
        float angle = ((now - spinnerStartTime) % 1200L) / 1200f * 360f;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);

        canvas.save();
        canvas.rotate(angle, cx, cy);

        int segments = 8;

        for (int i = 0; i < segments; i++) {
            float a0 = (float) Math.toRadians(i * 45f + 5f);
            float a1 = (float) Math.toRadians(i * 45f + 32f);

            float alphaBase = 0.10f + (i / 7f) * 0.70f;
            int alpha = (int) (255f * alphaBase * alphaFactor);

            p.setColor(Color.argb(alpha, 78, 192, 222));

            Path path = new Path();

            path.moveTo(
                    cx + (float) Math.cos(a0) * innerR,
                    cy + (float) Math.sin(a0) * innerR
            );

            path.lineTo(
                    cx + (float) Math.cos(a0) * outerR,
                    cy + (float) Math.sin(a0) * outerR
            );

            path.lineTo(
                    cx + (float) Math.cos(a1) * outerR,
                    cy + (float) Math.sin(a1) * outerR
            );

            path.lineTo(
                    cx + (float) Math.cos(a1) * innerR,
                    cy + (float) Math.sin(a1) * innerR
            );

            path.close();

            canvas.drawPath(path, p);
        }

        canvas.restore();

        invalidate();
    }

    // Progrees bar
    private void drawProgressPill(Canvas canvas, RectF guideRect, float progress) {
        float w = getWidth();
        float h = getHeight();

        progress = Math.max(0f, Math.min(1f, progress));

        float barW = w * 0.62f;
        float barH = h * 0.012f;
        float left = (w - barW) / 2f;
        float top = guideRect.bottom + h * 0.075f;
        float right = left + barW;
        float bottom = top + barH;
        float viewW = getWidth();

        RectF bg = new RectF(left, top, right, bottom);
        float radius = barH / 2f;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        // fondo/borde
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(65, 255, 255, 255));
        canvas.drawRoundRect(bg, radius, radius, p);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f);
        p.setColor(Color.argb(210, 255, 255, 255));
        canvas.drawRoundRect(bg, radius, radius, p);

        // Calcular el porcentaje
        int percent = requiredStableFrames > 0
                ? Math.max(0, Math.min(100, (int) ((stableCounter * 100f) / requiredStableFrames)))
                : 0;

        String label = percent + "%";

        // Configurar texto
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 👇 calcular centro vertical correctamente
        Paint.FontMetrics fm = textPaint.getFontMetrics();


        // centro de la barra
        float textX = bg.centerX();
        // 👇 esto lo coloca justo encima de la barra
        float textY = bg.top - 12f - fm.descent;

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(getWidth() * 0.04f);
        textPaint.setFakeBoldText(true);

        // dibujar
        canvas.drawText(label, textX, textY, textPaint);

        // progreso con degradado simple simulado
        float progressRight = left + (barW * progress);
        if (progressRight > left) {
            RectF fill = new RectF(left, top, progressRight, bottom);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(230, 78, 192, 222));
            canvas.drawRoundRect(fill, radius, radius, p);

            // brillo al final
            p.setColor(Color.argb(110, 255, 255, 255));
            float glowW = Math.min(barW * 0.12f, progressRight - left);
            RectF glow = new RectF(progressRight - glowW, top, progressRight, bottom);
            canvas.drawRoundRect(glow, radius, radius, p);
        }
    }

    // Fondo con circulo de circulo
    private void drawBackgroundHole(Canvas canvas, RectF holeRect) {
        float w = getWidth();
        float h = getHeight();

        Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(0xAA061B4B); // fondo azul oscuro con transparencia
        maskPaint.setStyle(Paint.Style.FILL);

        float radius = holeRect.width() * 0.07f;

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        // pantalla completa
        path.addRect(0, 0, w, h, Path.Direction.CW);

        // orificio transparente
        path.addRoundRect(holeRect, radius, radius, Path.Direction.CCW);

        canvas.drawPath(path, maskPaint);
    }

    // Dibujo de Marco
    private void drawFancyRoundedCorners(Canvas canvas, RectF r, Paint p) {
        float radius = r.width() * 0.06f; // editar radio del vorde
        float len = r.width() * 0.20f;

        Path path = new Path();

        // TOP LEFT
        path.moveTo(r.left, r.top + radius);
        path.quadTo(r.left, r.top, r.left + radius, r.top);
        path.lineTo(r.left + len, r.top);

        path.moveTo(r.left, r.top + radius);
        path.lineTo(r.left, r.top + len);

        // TOP RIGHT
        path.moveTo(r.right - radius, r.top);
        path.quadTo(r.right, r.top, r.right, r.top + radius);
        path.lineTo(r.right, r.top + len);

        path.moveTo(r.right - len, r.top);
        path.lineTo(r.right - radius, r.top);

        // BOTTOM LEFT
        path.moveTo(r.left, r.bottom - radius);
        path.quadTo(r.left, r.bottom, r.left + radius, r.bottom);
        path.lineTo(r.left + len, r.bottom);

        path.moveTo(r.left, r.bottom - len);
        path.lineTo(r.left, r.bottom - radius);

        // BOTTOM RIGHT
        path.moveTo(r.right - radius, r.bottom);
        path.quadTo(r.right, r.bottom, r.right, r.bottom - radius);
        path.lineTo(r.right, r.bottom - len);

        path.moveTo(r.right - len, r.bottom);
        path.lineTo(r.right - radius, r.bottom);

        canvas.drawPath(path, p);
    }

    // Barra de scaneo
    private void drawScanBar(Canvas canvas, RectF guideRect, float alphaFactor) {
        if (!scanEnabled) return;

        long now = System.currentTimeMillis();
        float cycleMs = 3000f;

        float t = ((now - scanStartTime) % (long) cycleMs) / cycleMs;

        // sube y baja: 0 -> 1 -> 0
        float wave = (float) (0.5f - 0.38f * Math.cos(t * 2f * Math.PI));

        float y = guideRect.top + (guideRect.height() * wave);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);

        float stroke = getWidth() * 0.010f;
        float margin = guideRect.width() * 0.10f;

        // rastro real: posiciones anteriores de la animación
        int trailCount = 10;
        float trailStep = 0.006f;

        for (int i = trailCount; i >= 1; i--) {
            float pastT = t - (i * trailStep);
            if (pastT < 0f) pastT += 1f;

            float pastWave = (float) (0.5f - 0.38f * Math.cos(pastT * 2f * Math.PI));
            float pastY = guideRect.top + (guideRect.height() * pastWave);

            float factor = 1f - (i / (float) trailCount);

            //int alpha = (int) (20 + 80 * factor);
            int alpha = (int) ((20 + 80 * factor) * alphaFactor);

            // aquí crece el grosor hacia atrás
            float dynamicStroke = stroke * (.9f + (factor * 1.2f));

            p.setStrokeWidth(dynamicStroke);
            // p.setColor(Color.argb(alpha, 150, 175, 170));
            p.setColor(Color.argb((int)(180 * alphaFactor), 150, 175, 170));

            canvas.drawLine(
                    guideRect.left + margin,
                    pastY,
                    guideRect.right - margin,
                    pastY,
                    p
            );
        }

        // barra principal
        p.setStrokeWidth(stroke);
        p.setColor(Color.argb((int)(180 * alphaFactor), 53, 214, 190));

        canvas.drawLine(
                guideRect.left + margin,
                y,
                guideRect.right - margin,
                y,
                p
        );

        // brillo central opcional
        p.setStrokeWidth(stroke * 0.45f);
        p.setColor(Color.argb(200, 214, 245, 190));

        canvas.drawLine(
                guideRect.left + margin,
                y,
                guideRect.right - margin,
                y,
                p
        );

        invalidate(); // mantiene la animación viva
    }

    // Texto de verificacion
    private void drawVerificationText(Canvas canvas, float cx, float cy, float radius) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(radius * 0.22f);
        p.setColor(0xFF35D6BE);

        RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawArc(oval, -90, 270, false, p);
    }

    // Overlay de Develop
    private void drawSuccessOverlay(Canvas canvas, float w, float h) {
        Paint bg = new Paint();
        bg.setColor(0xAA061B4B);
        bg.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, w, h, bg);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFFFFFFFF);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(w * 0.012f);

        float cx = w / 2f;
        float cy = h * 0.38f;
        float radius = w * 0.22f;

        canvas.drawCircle(cx, cy, radius, p);

        android.graphics.Path check = new android.graphics.Path();
        check.moveTo(cx - radius * 0.45f, cy);
        check.lineTo(cx - radius * 0.12f, cy + radius * 0.30f);
        check.lineTo(cx + radius * 0.50f, cy - radius * 0.35f);

        canvas.drawPath(check, p);
    }

    private void drawNewVisualLayer(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();

        RectF faceGuide = getMainGuideRect(w, h);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(w * 0.0095f); // Grosor de linea

        paint.setAntiAlias(true);

        if (overlayState == OverlayState.SUCCESS) {
            drawSuccessOverlay(canvas, w, h);
            return;
        }

        if (overlayState == OverlayState.ERROR) {
            paint.setColor(0xFFFF5A5A);
        } else if (overlayState == OverlayState.VERIFYING || alignedNow) {
            paint.setColor(0xFF35D6BE);
        } else {
            paint.setColor(0xFFFFFFFF);
        }

        //drawCornerFrame(canvas, faceGuide, paint);
        drawFancyRoundedCorners(canvas, faceGuide, paint);

        if (overlayState == OverlayState.VERIFYING) {
            //drawLoader(canvas, w / 2f, faceGuide.bottom + h * 0.08f, w * 0.07f);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float viewW = getWidth();
        float viewH = getHeight();

        RectF faceGuide = getMainGuideRect(viewW, viewH);

        // Primera capa: fondo con orificio
        drawBackgroundHole(canvas, faceGuide);

        //drawScanBar(canvas, faceGuide);
        updateScanAlpha();

        float progress = requiredStableFrames > 0
                ? Math.max(0f, Math.min(1f, stableCounter / (float) requiredStableFrames))
                : 0f;

        if (!alignedNow && overlayState != OverlayState.VERIFYING) {
            drawSegmentSpinner(canvas, faceGuide, scanAlpha);
        } else if (alignedNow && overlayState != OverlayState.VERIFYING) {
            drawProgressPill(canvas, faceGuide, progress);
        }

        if (scanAlpha > 0.02f) {
            drawScanBar(canvas, faceGuide, scanAlpha);
        }

        float previewW = Math.max(previewSize.getWidth(), 1);
        float previewH = Math.max(previewSize.getHeight(), 1);

        float overlayWidth = viewW * 0.60f;
        float overlayHeight = overlayWidth * 1.3f;
        float left = (viewW - overlayWidth) / 2f;
        float top = (viewH - overlayHeight) / 2.67f + (guideOffsetYRatio * viewH);

        RectF guideRect = new RectF(left, top, left + overlayWidth, top + overlayHeight);


        int guideColor = Color.RED;
        alignedNow = false;

        if (faceBox != null && frameSize.getWidth() > 0 && frameSize.getHeight() > 0) {
            float frameW = frameSize.getWidth();
            float frameH = frameSize.getHeight();

            float scale = Math.max(previewW / frameW, previewH / frameH);
            float dx = (previewW - frameW * scale) / 2f;
            float dy = (previewH - frameH * scale) / 2f;

            RectF mapped = new RectF(
                    (faceBox.left * scale + dx) * (viewW / previewW),
                    (faceBox.top * scale + dy) * (viewH / previewH),
                    (faceBox.right * scale + dx) * (viewW / previewW),
                    (faceBox.bottom * scale + dy) * (viewH / previewH)
            );

            RectF expandedGuide = new RectF(
                    guideRect.left - tolerancePx,
                    guideRect.top - tolerancePx,
                    guideRect.right + tolerancePx,
                    guideRect.bottom + tolerancePx
            );

            mapped.inset(35f, 0f);

            float faceHeightRatio = mapped.height() / viewH;
            boolean isInside = expandedGuide.contains(mapped);
            boolean isSizeOk = faceHeightRatio >= minSizeRatio && faceHeightRatio <= maxSizeRatio;

            alignedNow = isInside && isSizeOk;

            if (alignedNow) {
                guideColor = Color.GREEN;
            } else if (isInside) {
                guideColor = Color.YELLOW;
            } else {
                guideColor = Color.RED;
            }

            // Aqui dubuja los cuadros de la cara debug
            if (showDebugBoxes) {
                canvas.drawRect(mapped, facePaint);
            }
        }

        int percent = requiredStableFrames > 0
                ? Math.max(0, Math.min(100, (int) ((stableCounter * 100f) / requiredStableFrames)))
                : 0;

        String text = percent >= 100
                ? "Rostro alineado"
                : "Mantén la posición... " + percent + "%";

        if (showDebugBoxes) {
            guidePaint.setColor(guideColor);
            canvas.drawRect(guideRect, guidePaint);
            canvas.drawText(text, viewW / 2f, Math.max(50f, guideRect.top - 20f), textPaint);
        }

        drawNewVisualLayer(canvas);

    }
}