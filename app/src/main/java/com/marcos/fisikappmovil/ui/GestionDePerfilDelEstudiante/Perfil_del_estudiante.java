package com.marcos.fisikappmovil.ui.GestionDePerfilDelEstudiante;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder; // Importación para los nuevos diálogos
import com.marcos.fisikappmovil.model.ImagePickerHelper;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.repository.AuthRepository;
import com.marcos.fisikappmovil.remote.response.LoginResponse;
import com.marcos.fisikappmovil.data.repository.PerfilRepository;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.remote.response.PerfilResponse;
import com.marcos.fisikappmovil.security.CredentialVault;
import com.marcos.fisikappmovil.security.FaceEmbeddingCodec;
import com.marcos.fisikappmovil.security.FaceVault;
import com.marcos.fisikappmovil.ui.Autenticacion.FaceConsentActivity;
import com.marcos.fisikappmovil.ui.Autenticacion.Login;
import com.marcos.fisikappmovil.ui.faceNet.FaceEnrollActivity;

public class Perfil_del_estudiante extends AppCompatActivity implements ImagePickerHelper.ImagePickerListener {

    private ActivityResultLauncher<Intent> consentLauncher;
    private ActivityResultLauncher<Intent> enrollLauncher;
    private ImageView btnBack;

    private ShapeableImageView imgPerfil;
    private ImageView btnCambiarFoto;

    private ImagePickerHelper imagePickerHelper;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private TextView tvNombrePerfil;
    private TextView tvRolInstitucion;
    private TextView tvCorreoPerfil;
    private TextView tvIdentificacionPerfil;
    private TextView tvFechaNacimientoPerfil;
    private TextView tvEstadoCuentaPerfil;
    private TextView tvUltimoLoginPerfil;

    private Button btnCerrarSesionPerfil;
    private Button btnActivarRostro;
    private Button btnDesactivarRostro;

    private TokenManager tokenManager;
    private PerfilRepository perfilRepository;
    private PerfilResponse perfilActual;
    private AuthRepository authRepository;

    private boolean esperandoResultadoEnrolamiento = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_del_estudiante);

        imgPerfil = findViewById(R.id.imgPerfil);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);

        imagePickerHelper = new ImagePickerHelper(this, this);

        setupActivityResultLaunchers();

        btnCambiarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickerHelper.showImagePickerDialog();
            }
        });

        initDependencies();
        initViews();
        initLaunchers();
        initListeners();
        cargarPerfil();
    }


    private void setupActivityResultLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            imgPerfil.setImageURI(imageUri);
                        }
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            imgPerfil.setImageBitmap(imageBitmap);
                        }
                    }
                }
        );
    }

    @Override
    public void onGallerySelected() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onCameraSelected() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ImagePickerHelper.CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onCameraSelected();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initDependencies() {
        tokenManager = new TokenManager(this);
        perfilRepository = new PerfilRepository();
        authRepository = new AuthRepository();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvNombrePerfil = findViewById(R.id.tvNombrePerfil);
        tvRolInstitucion = findViewById(R.id.tvRolInstitucion);
        tvCorreoPerfil = findViewById(R.id.tvCorreoPerfil);
        tvIdentificacionPerfil = findViewById(R.id.tvIdentificacionPerfil);
        tvFechaNacimientoPerfil = findViewById(R.id.tvFechaNacimientoPerfil);
        tvEstadoCuentaPerfil = findViewById(R.id.tvEstadoCuentaPerfil);
        tvUltimoLoginPerfil = findViewById(R.id.tvUltimoLoginPerfil);
        btnCerrarSesionPerfil = findViewById(R.id.btnCerrarSesionPerfil);
        btnActivarRostro = findViewById(R.id.btnActivarRostro);
        btnDesactivarRostro = findViewById(R.id.btnDesactivarRostro);
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnActivarRostro.setOnClickListener(v -> iniciarFlujoReconocimientoFacial());
        btnCerrarSesionPerfil.setOnClickListener(v -> confirmarCerrarSesion());
        btnDesactivarRostro.setOnClickListener(v -> confirmarDesactivarRostro());
    }

    private void cargarPerfil() {
        String authHeader = tokenManager.getAuthorizationHeader();

        if (authHeader == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);

        perfilRepository.getPerfil(authHeader, result -> {
            setLoading(false);

            if (result.isSuccess()) {
                perfilActual = result.getData();
                mostrarPerfil(perfilActual);
            } else {
                Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarPerfil(PerfilResponse perfil) {
        if (perfil == null) return;

        tvNombrePerfil.setText(valueOrDefault(perfil.getNombre(), "Estudiante"));

        String rol = valueOrDefault(perfil.getRol(), "estudiante");
        String institucion = valueOrDefault(perfil.getInstitucion(), "Institución no registrada");

        tvRolInstitucion.setText(rol + " · " + institucion);
        tvCorreoPerfil.setText(valueOrDefault(perfil.getCorreo(), "Correo no registrado"));
        tvIdentificacionPerfil.setText("Identificación: " + valueOrDefault(perfil.getIdentificacion(), "--"));
        tvFechaNacimientoPerfil.setText("Fecha nacimiento: " + valueOrDefault(perfil.getFechaNacimiento(), "--"));
        tvEstadoCuentaPerfil.setText("Estado: " + (perfil.isEstado() ? "Activo" : "Inactivo"));
        tvUltimoLoginPerfil.setText("Último ingreso: " + valueOrDefault(perfil.getLastLogin(), "--"));

        actualizarEstadoFacial(perfil);
    }

    private void actualizarEstadoFacial(PerfilResponse perfil) {
        boolean consentimientoLocal = FaceVault.hasConsent(this);
        boolean embeddingLocal = FaceVault.hasEmbedding(this);

        if (consentimientoLocal && embeddingLocal) {
            btnActivarRostro.setText("Reemplazar rostro registrado");
            btnDesactivarRostro.setVisibility(View.VISIBLE);
        } else {
            btnActivarRostro.setText("Activar reconocimiento facial");
            btnDesactivarRostro.setVisibility(View.GONE);
        }
    }

    private void iniciarFlujoReconocimientoFacial() {
        if (perfilActual == null) {
            Toast.makeText(this, "Primero carga tu perfil", Toast.LENGTH_SHORT).show();
            return;
        }

        if (FaceVault.hasEmbedding(this)) {
            // CORREGIDO: Diálogo 1 de Material
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Rostro ya registrado")
                    .setMessage("Ya existe un rostro guardado en este dispositivo. ¿Deseas reemplazarlo?")
                    .setPositiveButton("Sí, reemplazar", (dialog, which) -> iniciarConsentimientoOEnroll())
                    .setNegativeButton("No", null)
                    .show();
        } else {
            iniciarConsentimientoOEnroll();
        }
    }

    private void iniciarConsentimientoOEnroll() {
        if (FaceVault.hasConsent(this)) {
            abrirEnrollFacial();
        } else {
            Intent intent = new Intent(this, FaceConsentActivity.class);
            consentLauncher.launch(intent);
        }
    }

    private void abrirEnrollFacial() {
        Intent intent = new Intent(this, FaceEnrollActivity.class);
        enrollLauncher.launch(intent);
    }

    private void initLaunchers() {
        consentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        abrirEnrollFacial();
                    } else {
                        Toast.makeText(this, "Consentimiento no aceptado", Toast.LENGTH_SHORT).show();
                        actualizarEstadoFacial(perfilActual);
                    }
                }
        );

        enrollLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        sincronizarRostroConBackend();
                    } else {
                        Toast.makeText(this, "Registro facial cancelado", Toast.LENGTH_SHORT).show();
                        actualizarEstadoFacial(perfilActual);
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (esperandoResultadoEnrolamiento) {
            esperandoResultadoEnrolamiento = false;

            if (FaceVault.hasConsent(this) && FaceVault.hasEmbedding(this)) {
                sincronizarRostroConBackend();
            } else {
                actualizarEstadoFacial(perfilActual);
            }
        }
    }

    private void sincronizarRostroConBackend() {
        float[] embedding = FaceVault.getEmbedding(this);

        if (!FaceEmbeddingCodec.isValidEmbedding(embedding)) {
            Toast.makeText(this, "Embedding facial inválido o no guardado", Toast.LENGTH_LONG).show();
            actualizarEstadoFacial(perfilActual);
            return;
        }

        String base64Embedding = FaceEmbeddingCodec.encode(embedding);
        String authHeader = tokenManager.getAuthorizationHeader();

        if (authHeader == null || authHeader.trim().isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        perfilRepository.actualizarPerfil(authHeader, base64Embedding, result -> {
            setLoading(false);

            if (result.isSuccess()) {
                perfilActual = result.getData();
                Toast.makeText(this, "Rostro sincronizado correctamente", Toast.LENGTH_SHORT).show();
                actualizarEstadoFacial(perfilActual);
                mostrarDialogoConfirmarPassword();
            } else {
                Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                actualizarEstadoFacial(perfilActual);
            }
        });
    }

    private void mostrarDialogoConfirmarPassword() {
        if (perfilActual == null || perfilActual.getCorreo() == null || perfilActual.getCorreo().trim().isEmpty()) {
            Toast.makeText(this, "No se pudo obtener el correo del perfil", Toast.LENGTH_LONG).show();
            return;
        }

        final android.widget.EditText inputPassword = new android.widget.EditText(this);
        inputPassword.setHint("Contraseña actual");
        inputPassword.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        inputPassword.setSingleLine(true);
        inputPassword.setPadding(40, 20, 40, 20);

        // CORREGIDO: Diálogo 2 usando MaterialAlertDialogBuilder completo y sin errores de sintaxis
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Activar ingreso facial")
                .setMessage("Confirma tu contraseña para activar el ingreso facial rápido en este dispositivo.")
                .setView(inputPassword)
                .setPositiveButton("Confirmar", null)
                .setNegativeButton("Ahora no", (d, which) -> {
                    Toast.makeText(
                            this,
                            "Rostro registrado. El ingreso facial rápido queda pendiente.",
                            Toast.LENGTH_LONG
                    ).show();
                    actualizarEstadoFacial(perfilActual);
                })
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(v -> {
                String password = inputPassword.getText().toString();

                if (password.trim().isEmpty()) {
                    inputPassword.setError("Ingresa tu contraseña");
                    return;
                }

                dialog.dismiss();
                confirmarPasswordYGuardarCredenciales(password);
            });
        });

        dialog.show();
    }

    private void confirmarPasswordYGuardarCredenciales(String password) {
        if (perfilActual == null) {
            Toast.makeText(this, "Perfil no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        String correo = perfilActual.getCorreo();

        if (correo == null || correo.trim().isEmpty()) {
            Toast.makeText(this, "Correo no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        authRepository.login(correo, password, result -> {
            setLoading(false);

            if (!result.isSuccess()) {
                Toast.makeText(
                        this,
                        "Contraseña incorrecta. No se activó el ingreso facial rápido.",
                        Toast.LENGTH_LONG
                ).show();
                actualizarEstadoFacial(perfilActual);
                return;
            }

            LoginResponse response = result.getData();

            if (response == null || !response.hasValidAccessToken()) {
                Toast.makeText(this, "No se pudo validar la sesión", Toast.LENGTH_LONG).show();
                actualizarEstadoFacial(perfilActual);
                return;
            }

            tokenManager.saveTokens(
                    response.getAccessToken(),
                    response.getRefreshToken()
            );

            if (response.getUser() != null) {
                tokenManager.saveUserData(
                        response.getUser().getNombre(),
                        response.getUser().getCorreo(),
                        response.getUser().getRol()
                );
            }

            CredentialVault.saveCredentials(this, correo, password);

            Toast.makeText(
                    this,
                    "Ingreso facial rápido activado en este dispositivo",
                    Toast.LENGTH_LONG
            ).show();

            actualizarEstadoFacial(perfilActual);
        });
    }

    private void confirmarDesactivarRostro() {
        // CORREGIDO: Diálogo 3 de Material
        new MaterialAlertDialogBuilder(this)
                .setTitle("Desactivar reconocimiento facial")
                .setMessage("Se eliminará el rostro guardado en este dispositivo y se desactivará el acceso facial en tu cuenta.")
                .setPositiveButton("Desactivar", (dialog, which) -> desactivarRostro())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void desactivarRostro() {
        String authHeader = tokenManager.getAuthorizationHeader();

        if (authHeader == null || authHeader.trim().isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        perfilRepository.desactivarReconocimientoFacial(authHeader, result -> {
            setLoading(false);

            if (result.isSuccess()) {
                FaceVault.clearAll(this);
                CredentialVault.clearCredentials(this);

                perfilActual = result.getData();

                Toast.makeText(this, "Reconocimiento facial desactivado", Toast.LENGTH_SHORT).show();
                actualizarEstadoFacial(perfilActual);
            } else {
                Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                actualizarEstadoFacial(perfilActual);
            }
        });
    }

    private void confirmarCerrarSesion() {
        // CORREGIDO: Diálogo 4 de Material
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas cerrar tu sesión actual?")
                .setPositiveButton("Cerrar sesión", (dialog, which) -> cerrarSesion())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cerrarSesion() {
        tokenManager.clearSession();

        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        btnActivarRostro.setEnabled(!loading);
        btnDesactivarRostro.setEnabled(!loading);
    }

    private String valueOrDefault(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }
}