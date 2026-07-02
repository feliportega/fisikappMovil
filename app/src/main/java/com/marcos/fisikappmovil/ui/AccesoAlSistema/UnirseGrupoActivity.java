package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.repository.GrupoJoinRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UnirseGrupoActivity extends AppCompatActivity {

    private static final String TAG = "UnirseGrupoActivity";
    private static final int CAMERA_REQUEST_CODE = 5010;

    private FrameLayout qrContainer;
    private PreviewView previewQr;
    //private ImageView imgQrMask;
    private QrGuideOverlayView qrGuideOverlay;
    private LinearLayout btnQrLarge;
    private TextView tvQrStatus;

    private EditText edtCodigoGrupo;
    private Button btnUnirseGrupo;
    private TextView tvEstadoUnion;

    private GrupoJoinRepository repository;

    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis imageAnalysis;
    private BarcodeScanner barcodeScanner;

    private volatile boolean scannerActive = false;
    private volatile boolean hasDetected = false;
    private volatile boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unirse_grupo);

        repository = new GrupoJoinRepository();

        initViews();
        initScanner();
        initListeners();
    }

    private void initViews() {
        qrContainer = findViewById(R.id.qrContainer);
        previewQr = findViewById(R.id.previewQr);
        //imgQrMask = findViewById(R.id.imgQrMask);
        qrGuideOverlay = findViewById(R.id.qrGuideOverlay);
        btnQrLarge = findViewById(R.id.btnQrLarge);
        tvQrStatus = findViewById(R.id.tvQrStatus);

        edtCodigoGrupo = findViewById(R.id.edtCodigoGrupo);
        btnUnirseGrupo = findViewById(R.id.btnUnirseGrupo);
        tvEstadoUnion = findViewById(R.id.tvEstadoUnion);
    }

    private void initScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();

        barcodeScanner = BarcodeScanning.getClient(options);
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void initListeners() {
        btnQrLarge.setOnClickListener(v -> activateQrScanner());

        btnUnirseGrupo.setOnClickListener(v -> intentarUnirse());
    }

    private void activateQrScanner() {
        if (scannerActive) {
            return;
        }

        if (!hasCameraPermission()) {
            requestCameraPermission();
            return;
        }

        scannerActive = true;
        hasDetected = false;

        previewQr.setVisibility(View.VISIBLE);

        qrGuideOverlay.setVisibility(View.VISIBLE);
        qrGuideOverlay.setState(QrGuideOverlayView.QrState.SCANNING);
        //imgQrMask.setVisibility(View.VISIBLE);
        //tvQrStatus.setVisibility(View.VISIBLE);
        //tvQrStatus.setText("Ubica el QR dentro del área");

        fadeOutQrButton();

        startCamera();
    }

    private void fadeOutQrButton() {
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(350);
        fadeOut.setFillAfter(true);

        btnQrLarge.startAnimation(fadeOut);

        btnQrLarge.postDelayed(() -> {
            btnQrLarge.setVisibility(View.GONE);
        }, 350);
    }

    private void resetQrButton() {
        btnQrLarge.clearAnimation();
        btnQrLarge.setAlpha(1f);
        btnQrLarge.setVisibility(View.VISIBLE);

        previewQr.setVisibility(View.GONE);

        qrGuideOverlay.setVisibility(View.GONE);
        qrGuideOverlay.setState(QrGuideOverlayView.QrState.IDLE);
        //imgQrMask.setVisibility(View.GONE);
        //tvQrStatus.setVisibility(View.GONE);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            activateQrScanner();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewQr.getSurfaceProvider());

                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeQrFrame);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                );

            } catch (Exception e) {
                Log.e(TAG, "Error iniciando cámara QR", e);
                tvEstadoUnion.setText("Error iniciando cámara");
                stopQrScanner();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @ExperimentalGetImage
    private void analyzeQrFrame(@NonNull ImageProxy imageProxy) {
        if (!scannerActive || hasDetected || isProcessing) {
            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        isProcessing = true;

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!scannerActive || hasDetected) {
                        return;
                    }

                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();
                        Rect box = barcode.getBoundingBox();

                        if (rawValue == null || rawValue.trim().isEmpty() || box == null) {
                            continue;
                        }

                        if (!isQrInsideOverlayArea(box, imageProxy.getWidth(), imageProxy.getHeight())) {
                            runOnUiThread(() -> qrGuideOverlay.setState(QrGuideOverlayView.QrState.ERROR));
                            runOnUiThread(() -> tvQrStatus.setText("Centra el QR dentro del área"));
                            continue;
                        }

                        hasDetected = true;

                        String codigo = normalizeQrValue(rawValue);

                        runOnUiThread(() -> {
                            qrGuideOverlay.setState(QrGuideOverlayView.QrState.FOUND);
                            edtCodigoGrupo.setText(codigo);
                            tvEstadoUnion.setText("Código QR detectado");

                            qrGuideOverlay.postDelayed(() -> {
                                stopQrScanner();
                            }, 500);
                        });

                        break;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error escaneando QR", e);
                })
                .addOnCompleteListener(task -> {
                    isProcessing = false;
                    imageProxy.close();
                });
    }

    private boolean isQrInsideTargetArea(Rect qrBox, int imageWidth, int imageHeight) {
        /*
         * Área objetivo aproximada en coordenadas de imagen.
         * Centro horizontal: 50%
         * Centro vertical: 42%
         * Tamaño: 55% del ancho y 45% de la altura.
         *
         * Luego, si quieres hacerlo más preciso, hacemos una View custom
         * que entregue exactamente el rectángulo de la máscara.
         */

        int targetWidth = (int) (imageWidth * 0.55f);
        int targetHeight = (int) (imageHeight * 0.45f);

        int centerX = imageWidth / 2;
        int centerY = (int) (imageHeight * 0.42f);

        int left = centerX - targetWidth / 2;
        int top = centerY - targetHeight / 2;
        int right = centerX + targetWidth / 2;
        int bottom = centerY + targetHeight / 2;

        int qrCenterX = qrBox.centerX();
        int qrCenterY = qrBox.centerY();

        return qrCenterX >= left
                && qrCenterX <= right
                && qrCenterY >= top
                && qrCenterY <= bottom;
    }

    private String normalizeQrValue(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim();

        // Si el QR luego trae una URL:
        // https://fisikapp.com/grupo?codigo=ABC123
        // aquí extraemos solo ABC123.
        return value;
    }

    private void intentarUnirse() {
        String codigo = edtCodigoGrupo.getText().toString();

        setLoading(true, "Validando código...");

        repository.unirseGrupo(codigo, result -> {
            setLoading(false, "");

            if (result.isSuccess()) {
                tvEstadoUnion.setText("Te uniste correctamente al grupo.");
                setResult(RESULT_OK);
                finish();
            } else {
                tvEstadoUnion.setText(result.getErrorMessage());
            }
        });
    }

    private void setLoading(boolean loading, String message) {
        btnUnirseGrupo.setEnabled(!loading);
        btnQrLarge.setEnabled(!loading);

        if (message != null) {
            tvEstadoUnion.setText(message);
        }
    }

    private void stopQrScanner() {
        scannerActive = false;
        isProcessing = false;

        try {
            if (imageAnalysis != null) {
                imageAnalysis.clearAnalyzer();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando analyzer QR", e);
        }

        try {
            if (cameraProvider != null) {
                cameraProvider.unbindAll();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando cámara QR", e);
        }

        resetQrButton();
    }

    @Override
    protected void onDestroy() {
        stopQrScanner();

        try {
            if (barcodeScanner != null) {
                barcodeScanner.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando BarcodeScanner", e);
        }

        try {
            if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
                cameraExecutor.shutdown();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando cameraExecutor QR", e);
        }

        super.onDestroy();
    }

    private boolean isQrInsideOverlayArea(Rect qrBox, int imageWidth, int imageHeight) {
        if (qrGuideOverlay == null || qrGuideOverlay.getWidth() == 0 || qrGuideOverlay.getHeight() == 0) {
            return false;
        }

        RectF scanRectView = qrGuideOverlay.getScanRect();

        float scaleX = imageWidth / (float) qrGuideOverlay.getWidth();
        float scaleY = imageHeight / (float) qrGuideOverlay.getHeight();

        RectF scanRectImage = new RectF(
                scanRectView.left * scaleX,
                scanRectView.top * scaleY,
                scanRectView.right * scaleX,
                scanRectView.bottom * scaleY
        );

        float qrCenterX = qrBox.centerX();
        float qrCenterY = qrBox.centerY();

        return scanRectImage.contains(qrCenterX, qrCenterY);
    }
}