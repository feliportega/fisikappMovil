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
import android.widget.Button;
import android.widget.ProgressBar;
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

import com.dcl.facesdk.FaceSdk;
import com.dcl.facesdk.FaceSdkResult;
import com.google.common.util.concurrent.ListenableFuture;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.facenet.FaceSdkBridge;
import com.marcos.fisikappmovil.security.FaceVault;
import com.marcos.fisikappmovil.ui.faceNet.utils.FaceEmbeddingUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceEnrollActivity extends AppCompatActivity {

    private static final String TAG = "FaceEnrollActivity";
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int REQUIRED_STABLE_FRAMES = 60;

    private FaceGuideOverlayView overlayView;
    private PreviewView previewView;

    private TextView tvStatusTitle;
    private TextView tvStatusMsg;
    private TextView tvSubtitle;
    //private TextView tvPercent;
    //private ProgressBar progressBar;
    private Button btnCancel;

    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis imageAnalysis;
    private ImageCapture imageCapture;

    private FaceSdk faceSdk;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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

    // Doble captura
    private int captureStep = 1;
    private float[] embeddingStep1 = null;
    private float[] embeddingStep2 = null;
    private boolean waitingForReposition = false;
    private boolean faceLeftGuideAfterFirstCapture = false;

    private static final long ENROLL_TIMEOUT_MS = 30000L;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());

    private final Runnable timeoutRunnable = () -> {
        if (!hasCompleted && !isClosing) {
            Toast.makeText(FaceEnrollActivity.this, "Tiempo agotado. Intenta nuevamente", Toast.LENGTH_SHORT).show();
            safeCloseAndFinish();
        }
    };

    private final Runnable stabilityLoop = new Runnable() {
        @Override
        public void run() {
            if (isClosing || hasCompleted) return;

            // Mientras espera reubicación para segunda captura, no acumula progreso
            if (waitingForReposition) {
                stableCounter = 0;
            } else if (aligned && !verifying) {
                stableCounter += 2;
            } else if (!verifying) {
                stableCounter = Math.max(0, stableCounter - 3);
            }

            stableCounter = Math.max(0, Math.min(stableCounter, REQUIRED_STABLE_FRAMES));

            //int percent = (int) ((stableCounter / (float) REQUIRED_STABLE_FRAMES) * 100f);
            //progressBar.setProgress(percent);
            //tvPercent.setText(percent + "%");

            // Estados visuales
            if (spoofDetected) {
                tvStatusTitle.setText("Rostro falso");
                tvStatusMsg.setText("No fue posible validar el rostro");
            } else if (verifying) {
                if (captureStep == 1) {
                    tvStatusTitle.setText("Captura 1 de 2");
                    tvStatusMsg.setText("Procesando primera foto...");
                } else {
                    tvStatusTitle.setText("Captura 2 de 2");
                    tvStatusMsg.setText("Procesando segunda foto...");
                }
            } else if (captureStep == 1) {
                if (aligned) {
                    tvSubtitle.setText("Por favor, mira a la camara");
                    tvStatusTitle.setText("Captura 1 de 2");
                    tvStatusMsg.setText("Mantén la posición...");
                } else {
                    tvStatusTitle.setText("Captura 1 de 2");
                    tvStatusMsg.setText("Centra tu rostro en el marco");
                }
            } else {
                if (waitingForReposition) {
                    tvStatusTitle.setText("Primera captura lista");
                    tvStatusMsg.setText("Ahora mueve ligeramente tu rostro");
                } else if (aligned) {
                    tvStatusTitle.setText("Captura 2 de 2");
                    tvStatusMsg.setText("Mantén la posición...");
                } else {
                    tvStatusTitle.setText("Captura 2 de 2");
                    tvStatusMsg.setText("Vuelve a centrar tu rostro");
                }
            }

            // Lógica de captura
            if (spoofDetected) {
                if (stableCounter >= REQUIRED_STABLE_FRAMES && !verifying) {
                    stableCounter = 0;
                    verifying = false;
                    Toast.makeText(FaceEnrollActivity.this, "❌ Rostro falso detectado", Toast.LENGTH_LONG).show();
                }
            } else {
                if (captureStep == 1) {
                    if (stableCounter >= REQUIRED_STABLE_FRAMES && !verifying) {
                        verifying = true;
                        stableCounter = 0;
                        captureAndEnroll();
                    }
                } else if (captureStep == 2) {
                    if (waitingForReposition) {
                        // Esperando a que se mueva, no capturamos
                    } else {
                        if (stableCounter >= REQUIRED_STABLE_FRAMES && !verifying) {
                            verifying = true;
                            stableCounter = 0;
                            captureAndEnroll();
                        }
                    }
                }
            }

            mainHandler.postDelayed(this, 50);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_enroll);

        overlayView = findViewById(R.id.overlayView);
        previewView = findViewById(R.id.previewView);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusMsg = findViewById(R.id.tvStatusMsg);
        tvSubtitle = findViewById(R.id.tvSubtitle);
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
            tvStatusTitle.setText("Cancelando...");
            tvStatusMsg.setText("Cerrando registro facial");
            safeCloseAndFinish();
        });

        previewView.post(() -> previewSize = new Size(previewView.getWidth(), previewView.getHeight()));

        if (hasCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
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

                tvStatusTitle.setText("Captura 1 de 2");
                tvStatusMsg.setText("Enfoca tu rostro dentro del marco");
                //progressBar.setProgress(0);
                //tvPercent.setText("0%");

                mainHandler.post(stabilityLoop);
                timeoutHandler.postDelayed(timeoutRunnable, ENROLL_TIMEOUT_MS);

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

                            // Segunda captura: exigir que salga del marco y vuelva a entrar
                            if (captureStep == 2 && waitingForReposition && !aligned) {
                                faceLeftGuideAfterFirstCapture = true;
                            }

                            if (captureStep == 2
                                    && waitingForReposition
                                    && faceLeftGuideAfterFirstCapture
                                    && aligned) {
                                waitingForReposition = false;
                                stableCounter = 0;
                                tvSubtitle.setText("Por favor, mira a la camara");
                                //progressBar.setProgress(0);
                                //tvPercent.setText("0%");
                            }
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

    private void captureAndEnroll() {
        if (imageCapture == null || isClosing || hasCompleted) {
            verifying = false;
            return;
        }

        File photoFile = new File(getCacheDir(), "face_enroll_" + System.currentTimeMillis() + ".jpg");
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
                                    tvStatusTitle.setText("Error");
                                    tvStatusMsg.setText("No se pudo procesar la foto");
                                    verifying = false;
                                });
                                return;
                            }

                            FaceSdkResult result = FaceSdkBridge.processPhotoBlocking(faceSdk, fullRes);

                            if (result == null || result.getEmbedding() == null) {
                                runOnUiThread(() -> {
                                    tvStatusTitle.setText("No válido");
                                    tvStatusMsg.setText("Rostro no válido o spoof");
                                    verifying = false;
                                });
                                return;
                            }

                            float[] embedding = result.getEmbedding();

                            if (captureStep == 1) {
                                embeddingStep1 = embedding;
                                captureStep = 2;
                                waitingForReposition = true;
                                faceLeftGuideAfterFirstCapture = false;
                                verifying = false;
                                stableCounter = 0;

                                runOnUiThread(() -> {
                                    //progressBar.setProgress(0);
                                    //tvPercent.setText("0%");
                                    tvSubtitle.setText("Cambia de posicion");
                                    tvStatusTitle.setText("Primera captura lista");
                                    tvStatusMsg.setText("Ahora mueve ligeramente tu rostro");
                                    Toast.makeText(FaceEnrollActivity.this, "Primera captura completada", Toast.LENGTH_SHORT).show();
                                });

                            } else if (captureStep == 2) {
                                embeddingStep2 = embedding;

                                float[] finalEmbedding = FaceEmbeddingUtils.averageAndNormalize(
                                        embeddingStep1,
                                        embeddingStep2
                                );

                                if (finalEmbedding != null) {
                                    FaceVault.saveEmbedding(FaceEnrollActivity.this, finalEmbedding);
                                    hasCompleted = true;

                                    runOnUiThread(() -> {
                                        tvSubtitle.setText("Correcto");
                                        tvStatusTitle.setText("Registro exitoso");
                                        tvStatusMsg.setText("Rostro enrolado correctamente");
                                        //progressBar.setProgress(100);
                                        //tvPercent.setText("100%");
                                        Toast.makeText(
                                                FaceEnrollActivity.this,
                                                "Embedding combinado guardado en el dispositivo",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    });

                                    safeCloseAndFinish();
                                } else {
                                    runOnUiThread(() -> {
                                        tvStatusTitle.setText("Error");
                                        tvStatusMsg.setText("No se pudo combinar el rostro");
                                        verifying = false;
                                    });
                                }
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error en processPhoto", e);
                            runOnUiThread(() -> {
                                tvStatusTitle.setText("Error");
                                tvStatusMsg.setText("Error procesando rostro");
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
                            tvStatusTitle.setText("Error");
                            tvStatusMsg.setText("Error capturando foto");
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

        timeoutHandler.removeCallbacks(timeoutRunnable);
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
        timeoutHandler.removeCallbacks(timeoutRunnable);

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