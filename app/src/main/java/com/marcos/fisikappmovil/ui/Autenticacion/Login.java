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

    private void manejarLoginExitoso(String email, LoginResponse response) {
        String accessToken = response.getAccessToken();
        String refreshToken = response.getRefreshToken();

        tokenManager.saveTokens(accessToken, refreshToken);

        Log.d("AUTH", "Access guardado: " + tokenManager.getAccessToken());
        Log.d("AUTH", "Refresh guardado: " + tokenManager.getRefreshToken());
        Log.d("AUTH", "Header: " + tokenManager.getAuthorizationHeader());


        String nombreUsuario = obtenerNombreUsuario(response);

        Toast.makeText(
                Login.this,
                "¡Bienvenido " + nombreUsuario + "!",
                Toast.LENGTH_SHORT
        ).show();

        irAlDashboard(email, nombreUsuario);
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

    private void irAlDashboard(String email, String nombreUsuario) {
        Intent intent = new Intent(Login.this, Dashboard.class);
        intent.putExtra("USER_EMAIL", email);
        intent.putExtra("USER_NAME", nombreUsuario);
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
}