package com.marcos.fisikappmovil.ui.faceNet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.marcos.fisikappmovil.security.FaceVault;
import com.marcos.fisikappmovil.ui.faceNet.FaceGuideOverlayView;
import com.marcos.fisikappmovil.facenet.FaceSdkBridge;
import com.dcl.facesdk.FaceSdk;
import com.dcl.facesdk.FaceSdkResult;
import com.google.common.util.concurrent.ListenableFuture;
import com.marcos.fisikappmovil.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceVerifyActivity extends AppCompatActivity {

    private static final String TAG = "FaceVerifyActivity";
    private static final int CAMERA_REQUEST_CODE = 1002;

    private static final int REQUIRED_STABLE_FRAMES = 60;
    private static final float MATCH_THRESHOLD = 0.75f;

    private FaceGuideOverlayView overlayView;
    private VerifyResultOverlayView resultOverlay;
    private PreviewView previewView;

    private TextView tvVerifyStatus; // texto de verificacion
    private TextView tvStatus;

    private TextView tvTitle;
    private Button btnPassword;
    private TextView btnCancel;

    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis imageAnalysis;
    private ImageCapture imageCapture;

    private FaceSdk faceSdk;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable stabilityLoop = new Runnable() {
        @Override
        public void run() {
            if (isClosing || hasCompleted) return;

            if (aligned && !verifying) {
                stableCounter += 2;
            } else if (!verifying) {
                stableCounter = Math.max(0, stableCounter - 3);
            }

            stableCounter = Math.max(0, Math.min(stableCounter, REQUIRED_STABLE_FRAMES));

            int percent = (int) ((stableCounter / (float) REQUIRED_STABLE_FRAMES) * 100f);

            if (spoofDetected) {
                tvVerifyStatus.setText("Rostro falso detectado");
            } else if (verifying) {
                tvVerifyStatus.setText("Verificando...");
                overlayView.setOverlayState(FaceGuideOverlayView.OverlayState.VERIFYING);
            } else if (aligned) {
                //tvVerifyStatus.setText("Mantén la posición... " + percent + "%");
                tvVerifyStatus.setText("Mantén la posición...");
                tvStatus.setText("Por favor, mira a la camara");
                overlayView.setOverlayState(FaceGuideOverlayView.OverlayState.ALIGNING);
            } else {
                tvVerifyStatus.setText("Buscando rostro");
                overlayView.setOverlayState(FaceGuideOverlayView.OverlayState.SEARCHING);
            }

            if (spoofDetected) {
                if (stableCounter >= REQUIRED_STABLE_FRAMES && !verifying) {
                    stableCounter = 0;
                    verifying = false;
                    //Toast.makeText(FaceVerifyActivity.this, "❌ Rostro falso detectado", Toast.LENGTH_LONG).show();
                }
            } else {
                if (stableCounter >= REQUIRED_STABLE_FRAMES && !verifying) {
                    verifying = true;
                    stableCounter = 0;
                    captureAndVerify();
                }
            }

            mainHandler.postDelayed(this, 50);
        }
    };

    private volatile boolean isClosing = false;
    private volatile boolean isProcessing = false;
    private volatile boolean hasCompleted = false;
    private volatile boolean verifying = false;

    private boolean aligned = false;
    private boolean spoofDetected = false;
    private int stableCounter = 0;
    private int liveCounter = 0;
    private int spoofCounter = 0;

    private Size frameSize = new Size(0, 0);
    private Size previewSize = new Size(0, 0);
    private RectF faceBox = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        float[] storedEmbedding = FaceVault.getEmbedding(this);
        if (storedEmbedding == null || storedEmbedding.length == 0) {
            Toast.makeText(this, "No hay rostro enrolado en este dispositivo", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!FaceVault.hasConsent(this)) {
            Toast.makeText(this, "No hay consentimiento facial registrado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_face_verify);

        overlayView = findViewById(R.id.overlayView);
        previewView = findViewById(R.id.previewView);
        resultOverlay = findViewById(R.id.resultOverlay);

        tvTitle = findViewById(R.id.tvTitle);
        tvStatus = findViewById(R.id.tvStatus);
        tvVerifyStatus = findViewById(R.id.tvVerifyStatus);
        btnPassword = findViewById(R.id.btnPassword);
        btnCancel = findViewById(R.id.btnCancel);

        cameraExecutor = Executors.newSingleThreadExecutor();
        faceSdk = new FaceSdk(
                this,
                "spoof_model_scale_2_7.tflite",
                "spoof_model_scale_4_0.tflite",
                80,
                1
        );

        btnCancel.setOnClickListener(v -> {
            tvVerifyStatus.setText("Cancelando...");
            safeCloseAndFinish();
        });

        btnPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Volver a login con contraseña", Toast.LENGTH_SHORT).show();
            safeCloseAndFinish();
        });

        previewView.post(() -> previewSize = new Size(previewView.getWidth(), previewView.getHeight()));

        if (hasCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void setControlsEnabled(boolean enabled) {
        btnCancel.setEnabled(enabled);
        btnPassword.setEnabled(enabled);

        btnCancel.setAlpha(enabled ? 1f : 0.4f);
        btnPassword.setAlpha(enabled ? 1f : 0.4f);

        if (enabled){
            btnCancel.setVisibility(View.GONE);
            btnPassword.setVisibility(View.GONE);
        } else {
            btnCancel.setVisibility(View.INVISIBLE);
            btnPassword.setVisibility(View.INVISIBLE);
        }

        resultOverlay.setClickable(enabled);
        resultOverlay.setFocusable(enabled);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageAnalysis = new ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageAnalysis,
                        imageCapture
                );

                tvVerifyStatus.setText("Buscando rostro");
                mainHandler.post(stabilityLoop);

            } catch (Exception e) {
                Log.e(TAG, "Error iniciando cámara", e);
                Toast.makeText(this, "Error iniciando cámara", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeFrame(@NonNull ImageProxy imageProxy) {
        if (isClosing || hasCompleted) {
            try {
                imageProxy.close();
            } catch (Exception ignored) {}
            return;
        }

        if (isProcessing || verifying) {
            try {
                imageProxy.close();
            } catch (Exception ignored) {}
            return;
        }

        isProcessing = true;

        try {
            int rot = imageProxy.getImageInfo().getRotationDegrees();
            int srcW = (rot % 180 == 0) ? imageProxy.getWidth() : imageProxy.getHeight();
            int srcH = (rot % 180 == 0) ? imageProxy.getHeight() : imageProxy.getWidth();
            frameSize = new Size(srcW, srcH);

            FaceSdkBridge.analyzeFrameAsync(
                    faceSdk,
                    imageProxy,
                    true,
                    true,
                    false,
                    resultList -> {
                        if (isClosing || hasCompleted) {
                            isProcessing = false;
                            try {
                                imageProxy.close();
                            } catch (Exception ignored) {}
                            return;
                        }

                        FaceSdkResult first = (resultList != null && !resultList.isEmpty())
                                ? resultList.get(0)
                                : null;

                        runOnUiThread(() -> {
                            if (isClosing || hasCompleted || isDestroyed()) {
                                return;
                            }

                            faceBox = first != null ? new RectF(first.getBbox()) : null;

                            if (first != null && first.getSpoof() != null) {
                                boolean isLive = first.getSpoof().isLive();
                                if (isLive) {
                                    liveCounter++;
                                } else {
                                    spoofCounter++;
                                }

                                int total = liveCounter + spoofCounter;
                                if (total > 30) {
                                    spoofDetected = spoofCounter > (liveCounter * 1.5f);
                                    liveCounter = 0;
                                    spoofCounter = 0;
                                }
                            }

                            overlayView.updateData(
                                    faceBox,
                                    frameSize,
                                    previewSize,
                                    stableCounter,
                                    REQUIRED_STABLE_FRAMES
                            );

                            aligned = overlayView.isAlignedNow();
                        });

                        isProcessing = false;
                        try {
                            imageProxy.close();
                        } catch (Exception ignored) {}
                    },
                    error -> {
                        if (!isClosing) {
                            Log.e(TAG, "Error en analyzeFrame", error);
                        }
                        isProcessing = false;
                        try {
                            imageProxy.close();
                        } catch (Exception ignored) {}
                    }
            );

        } catch (Exception e) {
            Log.e(TAG, "Error lanzando analyzeFrameAsync", e);
            isProcessing = false;
            try {
                imageProxy.close();
            } catch (Exception ignored) {}
        }
    }


    private void captureAndVerify() {
        if (imageCapture == null || isClosing || hasCompleted) {
            verifying = false;
            return;
        }

        File photoFile = new File(getCacheDir(), "face_verify_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        try {
                            Bitmap fullRes = decodeAndPrepareBitmap(photoFile, true);
                            if (fullRes == null) {
                                runOnUiThread(() -> {
                                    tvVerifyStatus.setText("No se pudo procesar la foto");
                                    verifying = false;
                                });
                                return;
                            }

                            FaceSdkResult result = FaceSdkBridge.processPhotoBlocking(faceSdk, fullRes);


                            float[] capturedEmbedding = null;
                            try {
                                capturedEmbedding = (float[]) result.getClass().getMethod("getEmbedding").invoke(result);
                            } catch (Exception reflectionError) {
                                Log.e(TAG, "No se pudo leer getEmbedding()", reflectionError);
                            }

                            if (capturedEmbedding != null) {

                                float[] enrolledEmbedding = FaceVault.getEmbedding(FaceVerifyActivity.this);

                                // Re-validacion
                                if (enrolledEmbedding == null || enrolledEmbedding.length == 0) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(FaceVerifyActivity.this, "No hay embedding guardado", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                    return;
                                }

                                float similarity = FaceCompareUtils.cosineSimilarity(enrolledEmbedding, capturedEmbedding);
                                boolean match = similarity >= MATCH_THRESHOLD;



                                float finalSimilarity = similarity;
                                runOnUiThread(() -> {
                                    if (match) {
                                        hasCompleted = true;
                                        setControlsEnabled(false);
                                        overlayView.setVisibility(View.GONE);
                                        resultOverlay.showSuccess(
                                                "Bienvenido",
                                                "Identidad verificada correctamente"
                                        );

                                        tvTitle.setText("VERIFICADO");
                                        tvStatus.setText("");
                                        tvVerifyStatus.setText("");

                                        //Toast.makeText(FaceVerifyActivity.this, "Rostro reconocido", Toast.LENGTH_SHORT).show();

                                        mainHandler.postDelayed(() -> {
                                            safeCloseAndFinish();
                                            safeCloseAndFinish();
                                        }, 2200);

                                    } else {
                                        hasCompleted = true;
                                        setControlsEnabled(false);
                                        overlayView.setVisibility(View.GONE);
                                        resultOverlay.showError(
                                                "No coincide",
                                                "Usa contraseña o intenta nuevamente"
                                        );

                                        tvTitle.setText("VERIFICADO");
                                        tvStatus.setText("");
                                        tvVerifyStatus.setText("");


                                        //Toast.makeText(FaceVerifyActivity.this, "Rostro no coincide", Toast.LENGTH_SHORT).show();

                                        mainHandler.postDelayed(() -> {
                                            resultOverlay.hideResult();
                                            overlayView.setVisibility(View.VISIBLE);
                                            verifying = false;
                                            hasCompleted = false;
                                            stableCounter = 0;
                                            safeCloseAndFinish();
                                        }, 2200);

                                    }
                                });

                                //safeCloseAndFinish();
                            } else {
                                runOnUiThread(() -> {
                                    tvVerifyStatus.setText("Rostro no válido");
                                    verifying = false;
                                });
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error en processPhoto", e);
                            runOnUiThread(() -> {
                                tvVerifyStatus.setText("Error procesando rostro");
                                verifying = false;
                            });
                        } finally {
                            //noinspection ResultOfMethodCallIgnored
                            photoFile.delete();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Error capturando foto", exception);
                        runOnUiThread(() -> {
                            tvVerifyStatus.setText("Error capturando foto");
                            verifying = false;
                        });
                    }
                }
        );
    }

    private Bitmap decodeAndPrepareBitmap(File photoFile, boolean mirrorFrontCamera) {
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        if (bitmap == null) return null;

        try {
            ExifInterface exif = new ExifInterface(photoFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );

            Matrix matrix = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    break;
            }

            if (mirrorFrontCamera) {
                matrix.postScale(-1f, 1f, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
            }

            Bitmap transformed = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
            );

            if (transformed.getConfig() != Bitmap.Config.ARGB_8888) {
                transformed = transformed.copy(Bitmap.Config.ARGB_8888, false);
            }

            if (transformed != bitmap) {
                bitmap.recycle();
            }

            return transformed;

        } catch (IOException e) {
            Log.e(TAG, "Error leyendo EXIF", e);
            if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                return bitmap.copy(Bitmap.Config.ARGB_8888, false);
            }
            return bitmap;
        }
    }

    private void safeCloseAndFinish() {
        if (isClosing) return;
        isClosing = true;

        //timeoutHandler.removeCallbacks(timeoutRunnable);
        mainHandler.removeCallbacks(stabilityLoop);

        try {
            if (imageAnalysis != null) {
                imageAnalysis.clearAnalyzer();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando analyzer", e);
        }

        // Dar un pequeño margen para que termine cualquier análisis en curso
        mainHandler.postDelayed(() -> {
            try {
                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error cerrando cámara", e);
            }

            try {
                if (faceSdk != null) {
                    faceSdk.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error cerrando FaceSdk", e);
            }

            if (!isFinishing() && !isDestroyed()) {
                finish();
            }
        }, 250);
    }

    @Override
    protected void onDestroy() {
        isClosing = true;

        mainHandler.removeCallbacks(stabilityLoop);
        //timeoutHandler.removeCallbacks(timeoutRunnable);

        try {
            if (imageAnalysis != null) {
                imageAnalysis.clearAnalyzer();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando analyzer en onDestroy", e);
        }

        try {
            if (cameraProvider != null) {
                cameraProvider.unbindAll();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando cámara en onDestroy", e);
        }

        try {
            if (faceSdk != null) {
                faceSdk.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando FaceSdk en onDestroy", e);
        }

        try {
            if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
                cameraExecutor.shutdown();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando cameraExecutor en onDestroy", e);
        }

        super.onDestroy();
    }
}