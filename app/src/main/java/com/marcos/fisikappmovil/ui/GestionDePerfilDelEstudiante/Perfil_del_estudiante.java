package com.marcos.fisikappmovil.ui.GestionDePerfilDelEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.repository.PerfilRepository;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.remote.response.PerfilResponse;
import com.marcos.fisikappmovil.security.CredentialVault;
import com.marcos.fisikappmovil.security.FaceEmbeddingCodec;
import com.marcos.fisikappmovil.security.FaceVault;
import com.marcos.fisikappmovil.ui.Autenticacion.FaceConsentActivity;
import com.marcos.fisikappmovil.ui.faceNet.FaceEnrollActivity;

public class Perfil_del_estudiante extends AppCompatActivity {

    private ActivityResultLauncher<Intent> consentLauncher;
    private ActivityResultLauncher<Intent> enrollLauncher;
    private ImageView btnBack;
    private ImageView imgPerfil;

    private TextView tvNombrePerfil;
    private TextView tvRolInstitucion;
    private TextView tvCorreoPerfil;
    private TextView tvIdentificacionPerfil;
    private TextView tvFechaNacimientoPerfil;
    private TextView tvEstadoCuentaPerfil;
    private TextView tvUltimoLoginPerfil;
    private TextView tvEstadoFacialPerfil;

    private Button btnEditarPerfil;
    private Button btnActivarRostro;
    private Button btnDesactivarRostro;

    private TokenManager tokenManager;
    private PerfilRepository perfilRepository;

    private PerfilResponse perfilActual;

    private boolean esperandoResultadoEnrolamiento = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_del_estudiante);

        initDependencies();
        initViews();
        initLaunchers();
        initListeners();
        cargarPerfil();
    }

    private void initDependencies() {
        tokenManager = new TokenManager(this);
        perfilRepository = new PerfilRepository();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgPerfil = findViewById(R.id.imgPerfil);

        tvNombrePerfil = findViewById(R.id.tvNombrePerfil);
        tvRolInstitucion = findViewById(R.id.tvRolInstitucion);
        tvCorreoPerfil = findViewById(R.id.tvCorreoPerfil);
        tvIdentificacionPerfil = findViewById(R.id.tvIdentificacionPerfil);
        tvFechaNacimientoPerfil = findViewById(R.id.tvFechaNacimientoPerfil);
        tvEstadoCuentaPerfil = findViewById(R.id.tvEstadoCuentaPerfil);
        tvUltimoLoginPerfil = findViewById(R.id.tvUltimoLoginPerfil);
        tvEstadoFacialPerfil = findViewById(R.id.tvEstadoFacialPerfil);

        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnActivarRostro = findViewById(R.id.btnActivarRostro);
        btnDesactivarRostro = findViewById(R.id.btnDesactivarRostro);
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEditarPerfil.setOnClickListener(v -> {
            Toast.makeText(this, "Próximamente: edición de perfil", Toast.LENGTH_SHORT).show();
        });

        btnActivarRostro.setOnClickListener(v -> iniciarFlujoReconocimientoFacial());

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
        boolean consentimientoBackend = perfil != null && perfil.isAutorizacionDatos();
        boolean embeddingBackend = perfil != null && perfil.hasBackendFaceEmbedding();

        boolean consentimientoLocal = FaceVault.hasConsent(this);
        boolean embeddingLocal = FaceVault.hasEmbedding(this);
        boolean credencialesLocales = CredentialVault.hasCredentials(this);

        String estado =
                "Autorización servidor: " + boolText(consentimientoBackend) + "\n" +
                        "Rostro servidor: " + boolText(embeddingBackend) + "\n" +
                        "Autorización dispositivo: " + boolText(consentimientoLocal) + "\n" +
                        "Rostro dispositivo: " + boolText(embeddingLocal) + "\n" +
                        "Ingreso facial rápido: " + boolText(consentimientoLocal && embeddingLocal && credencialesLocales);

        tvEstadoFacialPerfil.setText(estado);

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
            new AlertDialog.Builder(this)
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

    private void abrirFlujoFacial() {
        esperandoResultadoEnrolamiento = true;

        Intent intent;

        if (FaceVault.hasConsent(this)) {
            intent = new Intent(this, FaceEnrollActivity.class);
        } else {
            intent = new Intent(this, FaceConsentActivity.class);
        }

        startActivity(intent);
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
            } else {
                Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                actualizarEstadoFacial(perfilActual);
            }
        });
    }

    private void confirmarDesactivarRostro() {
        new AlertDialog.Builder(this)
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

    private void setLoading(boolean loading) {
        btnEditarPerfil.setEnabled(!loading);
        btnActivarRostro.setEnabled(!loading);
        btnDesactivarRostro.setEnabled(!loading);
    }

    private String valueOrDefault(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }

    private String boolText(boolean value) {
        return value ? "Sí" : "No";
    }
}