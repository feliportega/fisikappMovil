package com.marcos.fisikappmovil.ui.Autenticacion;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.repository.AuthRepository;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.remote.response.LoginResponse;
import com.marcos.fisikappmovil.security.CredentialVault;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;
import com.marcos.fisikappmovil.ui.RecuperacionDeCuenta.RecuperarCuenta;

public class Login extends AppCompatActivity {

    private EditText edtCorreo;
    private EditText edtPassword;
    private TextView tvErrorBanner;
    private ImageView ivShowPassword;
    private Button btnRegistro;
    private Button btnRecuperar;
    private Button btnSesion;

    private TokenManager tokenManager;
    private AuthRepository authRepository;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initDependencies();
        initViews();
        initListeners();
    }

    private void initDependencies() {
        tokenManager = new TokenManager(this);
        authRepository = new AuthRepository();
    }

    private void initViews() {
        edtPassword = findViewById(R.id.editTextPassword);
        edtCorreo = findViewById(R.id.editTextTextEmailAddress);
        tvErrorBanner = findViewById(R.id.tvErrorBanner);
        ivShowPassword = findViewById(R.id.ivShowPassword);

        btnRegistro = findViewById(R.id.btnResgistrarse);
        btnRecuperar = findViewById(R.id.btnRecuperarc);
        btnSesion = findViewById(R.id.btnIniciarsesion);

        tvErrorBanner.setVisibility(View.GONE);
    }

    private void initListeners() {
        ivShowPassword.setOnClickListener(v -> togglePasswordVisibility());

        btnRegistro.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });

        btnRecuperar.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, RecuperarCuenta.class);
            startActivity(intent);
        });

        btnSesion.setOnClickListener(view -> intentarLogin());
    }

    private void intentarLogin() {
        String email = edtCorreo.getText().toString();
        String password = edtPassword.getText().toString();

        ocultarError();

        if (!validarCampos(email, password)) {
            return;
        }

        ejecutarLogin(email, password);
    }

    private boolean validarCampos(String email, String password) {
        boolean isValid = true;

        if (email == null || email.trim().isEmpty()) {
            edtCorreo.setError("Campo requerido");
            isValid = false;
        }

        if (password == null || password.trim().isEmpty()) {
            edtPassword.setError("Campo requerido");
            isValid = false;
        }

        return isValid;
    }

    private void ejecutarLogin(String email, String password) {
        setLoginEnabled(false);

        authRepository.login(email, password, result -> {
            setLoginEnabled(true);

            if (result.isSuccess()) {
                manejarLoginExitoso(email, result.getData());
            } else {
                mostrarError(result.getErrorMessage());
                edtPassword.setText("");
            }
        });
    }

    //Solo test String password -> manejarLoginExitoso(String email, String password, LoginResponse response)
    private void manejarLoginExitoso(String email, LoginResponse response) {
        if (response == null || !response.hasValidAccessToken()) {
            mostrarError("No fue posible iniciar sesión. Respuesta inválida.");
            return;
        }

        if (!esRolEstudiante(response)) {
            denegarAccesoPorRol(response);
            return;
        }

        tokenManager.saveTokens(
                response.getAccessToken(),
                response.getRefreshToken()
        );


        String nombreUsuario = obtenerNombreUsuario(response);

        if (response.getUser() != null) {
            tokenManager.saveUserData(
                    response.getUser().getNombre(),
                    response.getUser().getCorreo(),
                    response.getUser().getRol()
            );
        }

        Toast.makeText(
                Login.this,
                "¡Bienvenido " + nombreUsuario + "!",
                Toast.LENGTH_SHORT
        ).show();

        irAlDashboard();
    }

    private String obtenerNombreUsuario(LoginResponse response) {
        if (response != null
                && response.getUser() != null
                && response.getUser().getNombre() != null
                && !response.getUser().getNombre().trim().isEmpty()) {
            return response.getUser().getNombre();
        }
        return "Usuario";
    }

    private void irAlDashboard() {
        Intent intent = new Intent(Login.this, Dashboard.class);
        startActivity(intent);
        finish();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            ivShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
        } else {
            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            // Si luego tienes ícono de ocultar, lo colocamos aquí.
            // ivShowPassword.setImageResource(R.drawable.baseline_visibility_off_24);
        }

        isPasswordVisible = !isPasswordVisible;
        edtPassword.setSelection(edtPassword.getText().length());
    }

    private void mostrarError(String mensaje) {
        tvErrorBanner.setText(mensaje);
        tvErrorBanner.setVisibility(View.VISIBLE);
    }

    private void ocultarError() {
        tvErrorBanner.setVisibility(View.GONE);
    }

    private void setLoginEnabled(boolean enabled) {
        btnSesion.setEnabled(enabled);
        btnRegistro.setEnabled(enabled);
        btnRecuperar.setEnabled(enabled);

        if (enabled) {
            btnSesion.setText("Iniciar sesión");
        } else {
            btnSesion.setText("Iniciando...");
        }
    }

    private void denegarAccesoPorRol(LoginResponse response) {
        String rol = "desconocido";

        if (response != null && response.getUser() != null && response.getUser().getRol() != null) {
            rol = response.getUser().getRol();
        }

        tokenManager.clearSession();

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Acceso no permitido")
                .setMessage(
                        "Esta aplicación móvil está disponible solo para estudiantes.\n\n" +
                                "Tu rol actual es: " + rol + "."
                )
                //.setPositiveButton("Entendido", null)
                .setPositiveButton("Ir a la web", (dialog, which) -> abrirWeb())
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void abrirWeb() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://URL-DE-LA-WEB.com"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir la web", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean esRolEstudiante(LoginResponse response) {
        if (response == null || response.getUser() == null) {
            return false;
        }

        String rol = response.getUser().getRol();

        return rol != null && rol.trim().equalsIgnoreCase("estudiante");
    }
}