package com.marcos.fisikappmovil.ui.faceNet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class VerifyResultOverlayView extends View {

    private boolean visibleResult = false;
    private boolean success = false;
    private String title = "";
    private String message = "";

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public VerifyResultOverlayView(Context context) {
        super(context);
        init();
    }

    public VerifyResultOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerifyResultOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVisibility(GONE);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void showSuccess(String title, String message) {
        this.success = true;
        this.title = title;
        this.message = message;
        this.visibleResult = true;

        setAlpha(0f);
        setVisibility(VISIBLE);

        animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        invalidate();
    }

    public void showError(String title, String message) {
        this.success = false;
        this.title = title;
        this.message = message;
        this.visibleResult = true;

        setAlpha(0f);
        setVisibility(VISIBLE);

        animate()
                .alpha(1f)
                .setDuration(250)
                .start();

        invalidate();
    }

    public void hideResult() {
        animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    visibleResult = false;
                    setVisibility(GONE);
                })
                .start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!visibleResult) return;

        float w = getWidth();
        float h = getHeight();

        // fondo oscuro translúcido
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xCC061B4B);
        canvas.drawRect(0, 0, w, h, paint);

        float cx = w / 2f;
        float cy = h * 0.34f;
        float radius = w * 0.18f;

        int mainColor = success ? Color.rgb(53, 214, 190) : Color.rgb(255, 90, 90);

        // círculo
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(w * 0.012f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(mainColor);
        canvas.drawCircle(cx, cy, radius, paint);

        if (success) {
            drawCheck(canvas, cx, cy, radius, paint);
        } else {
            drawX(canvas, cx, cy, radius, paint);
        }

        // título
        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(w * 0.075f);
        canvas.drawText(title, cx, cy + radius + h * 0.09f, textPaint);

        // mensaje
        textPaint.setFakeBoldText(false);
        textPaint.setTextSize(w * 0.043f);
        textPaint.setColor(0xFFD8F0F0);
        canvas.drawText(message, cx, cy + radius + h * 0.14f, textPaint);
    }

    private void drawCheck(Canvas canvas, float cx, float cy, float radius, Paint p) {
        Path check = new Path();
        check.moveTo(cx - radius * 0.45f, cy);
        check.lineTo(cx - radius * 0.15f, cy + radius * 0.30f);
        check.lineTo(cx + radius * 0.50f, cy - radius * 0.35f);
        canvas.drawPath(check, p);
    }

    private void drawX(Canvas canvas, float cx, float cy, float radius, Paint p) {
        canvas.drawLine(cx - radius * 0.38f, cy - radius * 0.38f,
                cx + radius * 0.38f, cy + radius * 0.38f, p);

        canvas.drawLine(cx + radius * 0.38f, cy - radius * 0.38f,
                cx - radius * 0.38f, cy + radius * 0.38f, p);
    }
}
