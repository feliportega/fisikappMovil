package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.common.InputImage;
import com.marcos.fisikappmovil.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QrScannerActivity extends AppCompatActivity {

    public static final String EXTRA_QR_VALUE = "EXTRA_QR_VALUE";

    private static final String TAG = "QrScannerActivity";
    private static final int CAMERA_REQUEST_CODE = 5010;

    private PreviewView previewQr;
    private TextView tvQrStatus;
    private Button btnCancelarQr;

    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis imageAnalysis;
    private BarcodeScanner barcodeScanner;

    private volatile boolean hasDetected = false;
    private volatile boolean isClosing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_scanner);

        initViews();
        initScanner();

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnCancelarQr.setOnClickListener(v -> finishWithCancel());

        if (hasCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void initViews() {
        previewQr = findViewById(R.id.previewQr);
        tvQrStatus = findViewById(R.id.tvQrStatus);
        btnCancelarQr = findViewById(R.id.btnCancelarQr);
    }

    private void initScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();

        barcodeScanner = BarcodeScanning.getClient(options);
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
            startCamera();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            finishWithCancel();
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

                tvQrStatus.setText("Escanea el código QR del grupo");

            } catch (Exception e) {
                Log.e(TAG, "Error iniciando cámara QR", e);
                Toast.makeText(this, "Error iniciando cámara", Toast.LENGTH_SHORT).show();
                finishWithCancel();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @ExperimentalGetImage
    private void analyzeQrFrame(@NonNull ImageProxy imageProxy) {
        if (isClosing || hasDetected) {
            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (isClosing || hasDetected) {
                        return;
                    }

                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();

                        if (rawValue != null && !rawValue.trim().isEmpty()) {
                            hasDetected = true;
                            String codigo = normalizeQrValue(rawValue);

                            runOnUiThread(() -> {
                                tvQrStatus.setText("Código detectado");
                                finishWithResult(codigo);
                            });
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isClosing) {
                        Log.e(TAG, "Error escaneando QR", e);
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private String normalizeQrValue(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim();

        // Si el QR trae una URL tipo:
        // https://fisikapp.com/grupo?codigo=ABC123
        // aquí luego podemos extraer solo el parámetro codigo.
        return value;
    }

    private void finishWithResult(String codigo) {
        safeClose(() -> {
            Intent data = new Intent();
            data.putExtra(EXTRA_QR_VALUE, codigo);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void finishWithCancel() {
        safeClose(() -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void safeClose(Runnable afterClose) {
        if (isClosing) {
            return;
        }

        isClosing = true;

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

        try {
            if (barcodeScanner != null) {
                barcodeScanner.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando scanner QR", e);
        }

        try {
            if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
                cameraExecutor.shutdown();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando executor QR", e);
        }

        if (afterClose != null && !isDestroyed()) {
            afterClose.run();
        }
    }

    @Override
    protected void onDestroy() {
        safeClose(null);
        super.onDestroy();
    }
}