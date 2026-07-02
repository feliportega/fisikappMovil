package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class QrGuideOverlayView extends View {

    public enum QrState {
        IDLE,
        SCANNING,
        FOUND,
        ERROR
    }

    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF scanRect = new RectF();

    private QrState state = QrState.IDLE;

    public QrGuideOverlayView(Context context) {
        super(context);
        init();
    }

    public QrGuideOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QrGuideOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        dimPaint.setColor(Color.argb(145, 0, 0, 0));
        dimPaint.setStyle(Paint.Style.FILL);

        borderPaint.setColor(Color.argb(150, 255, 255, 255));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(2));

        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(dp(5));
        cornerPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(14));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setState(QrState state) {
        this.state = state;
        updateColors();
        invalidate();
    }

    private void updateColors() {
        switch (state) {
            case FOUND:
                cornerPaint.setColor(Color.rgb(70, 220, 120));
                borderPaint.setColor(Color.argb(180, 70, 220, 120));
                break;

            case ERROR:
                cornerPaint.setColor(Color.rgb(255, 90, 90));
                borderPaint.setColor(Color.argb(180, 255, 90, 90));
                break;

            case SCANNING:
                cornerPaint.setColor(Color.WHITE);
                borderPaint.setColor(Color.argb(150, 255, 255, 255));
                break;

            case IDLE:
            default:
                cornerPaint.setColor(Color.WHITE);
                borderPaint.setColor(Color.argb(120, 255, 255, 255));
                break;
        }
    }

    public RectF getScanRect() {
        return new RectF(scanRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        calculateScanRect();

        drawDimmedBackground(canvas);
        drawScanBorder(canvas);
        drawCorners(canvas);
        drawHintText(canvas);
    }

    private void calculateScanRect() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float size = Math.min(viewWidth * 0.68f, viewHeight * 0.62f);

        float left = (viewWidth - size) / 2f;
        float top = (viewHeight - size) / 2f - dp(8);
        float right = left + size;
        float bottom = top + size;

        scanRect.set(left, top, right, bottom);
    }

    private void drawDimmedBackground(Canvas canvas) {
        Path path = new Path();
        path.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);

        Path clearPath = new Path();
        clearPath.addRoundRect(scanRect, dp(22), dp(22), Path.Direction.CW);

        path.op(clearPath, Path.Op.DIFFERENCE);

        canvas.drawPath(path, dimPaint);
    }

    private void drawScanBorder(Canvas canvas) {
        canvas.drawRoundRect(scanRect, dp(22), dp(22), borderPaint);
    }

    private void drawCorners(Canvas canvas) {
        float length = dp(34);

        float left = scanRect.left;
        float top = scanRect.top;
        float right = scanRect.right;
        float bottom = scanRect.bottom;

        // Superior izquierda
        canvas.drawLine(left, top + length, left, top, cornerPaint);
        canvas.drawLine(left, top, left + length, top, cornerPaint);

        // Superior derecha
        canvas.drawLine(right - length, top, right, top, cornerPaint);
        canvas.drawLine(right, top, right, top + length, cornerPaint);

        // Inferior izquierda
        canvas.drawLine(left, bottom - length, left, bottom, cornerPaint);
        canvas.drawLine(left, bottom, left + length, bottom, cornerPaint);

        // Inferior derecha
        canvas.drawLine(right - length, bottom, right, bottom, cornerPaint);
        canvas.drawLine(right, bottom - length, right, bottom, cornerPaint);
    }

    private void drawHintText(Canvas canvas) {
        String text;

        switch (state) {
            case FOUND:
                text = "QR detectado";
                break;

            case ERROR:
                text = "Centra el código QR";
                break;

            case SCANNING:
                text = "Ubica el QR dentro del marco";
                break;

            case IDLE:
            default:
                text = "Toca QR Scan para activar";
                break;
        }

        float x = getWidth() / 2f;
        float y = scanRect.bottom + dp(34);

        canvas.drawText(text, x, y, textPaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}