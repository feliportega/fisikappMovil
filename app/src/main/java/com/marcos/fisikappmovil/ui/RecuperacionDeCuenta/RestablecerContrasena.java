package com.marcos.fisikappmovil.ui.RecuperacionDeCuenta;

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
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.remote.request.ResetPasswordRequest;
import com.marcos.fisikappmovil.ui.Autenticacion.Login;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestablecerContrasena extends AppCompatActivity {
    
    EditText etUid, etToken, etNuevaPass, etConfirmarPass;
    TextView tvErrorBanner;
    ImageView ivShowNewPassword, ivShowConfirmPasswordReset;
    Button btnRestablecer;
    String userEmail;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restablecer_contrasena);

        // Recuperar el correo enviado desde la pantalla anterior
        userEmail = getIntent().getStringExtra("user_email");

        // Inicializar vistas
        etUid = findViewById(R.id.etUid);
        etToken = findViewById(R.id.etToken);
        etNuevaPass = findViewById(R.id.etNuevaPassword);
        etConfirmarPass = findViewById(R.id.etConfirmarPassword);
        tvErrorBanner = findViewById(R.id.tvErrorBanner);
        ivShowNewPassword = findViewById(R.id.ivShowNewPassword);
        ivShowConfirmPasswordReset = findViewById(R.id.ivShowConfirmPasswordReset);
        btnRestablecer = findViewById(R.id.btnNuevacontraseña);

        // Si el correo es nulo, mostramos error crítico
        if (userEmail == null || userEmail.isEmpty()) {
            tvErrorBanner.setText("Error: Session expired. Go back and request a new link.");
            tvErrorBanner.setVisibility(View.VISIBLE);
        }

        ivShowNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNewPasswordVisibility();
            }
        });

        ivShowConfirmPasswordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleConfirmPasswordVisibility();
            }
        });

        btnRestablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarYRestablecer();
            }
        });
    }

    private void toggleNewPasswordVisibility() {
        if (isNewPasswordVisible) {
            etNuevaPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivShowNewPassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
        } else {
            etNuevaPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        isNewPasswordVisible = !isNewPasswordVisible;
        etNuevaPass.setSelection(etNuevaPass.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmarPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivShowConfirmPasswordReset.setImageResource(R.drawable.baseline_remove_red_eye_24);
        } else {
            etConfirmarPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        etConfirmarPass.setSelection(etConfirmarPass.getText().length());
    }

    private void validarYRestablecer() {
        String uid = etUid.getText().toString().trim();
        String token = etToken.getText().toString().trim();
        String password = etNuevaPass.getText().toString().trim();
        String confirmacion = etConfirmarPass.getText().toString().trim();

        tvErrorBanner.setVisibility(View.GONE);

        if (userEmail == null || userEmail.isEmpty()) {
            tvErrorBanner.setText("Error: Email is missing. Please try again from the start.");
            tvErrorBanner.setVisibility(View.VISIBLE);
            return;
        }

        if (uid.isEmpty() || token.isEmpty() || password.isEmpty() || confirmacion.isEmpty()) {
            tvErrorBanner.setText("All fields are required");
            tvErrorBanner.setVisibility(View.VISIBLE);
            return;
        }

        if (password.length() < 8) {
            tvErrorBanner.setText("Minimum 8 characters for password");
            tvErrorBanner.setVisibility(View.VISIBLE);
            return;
        }

        if (!password.equals(confirmacion)) {
            tvErrorBanner.setText("Passwords do not match");
            tvErrorBanner.setVisibility(View.VISIBLE);
            return;
        }

        enviarNuevaContrasena(uid, token, password);
    }

    private void enviarNuevaContrasena(String uid, String token, String password) {
        // Log para depuración - Revisar en Logcat de Android Studio
        Log.d("FISIKAPP_DEBUG", "Enviando: Email=" + userEmail + " UID=" + uid + " Token=" + token);

        ResetPasswordRequest request = new ResetPasswordRequest(userEmail, uid, token, password);
        FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);
        Call<Void> call = api.restablecerContrasena(request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RestablecerContrasena.this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RestablecerContrasena.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Si el servidor responde error 400 o similar
                    Log.e("FISIKAPP_DEBUG", "Error del servidor: " + response.code());
                    tvErrorBanner.setText("Error: Invalid or expired UID/Token.");
                    tvErrorBanner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FISIKAPP_DEBUG", "Error de red: " + t.getMessage());
                tvErrorBanner.setText("Network error: " + t.getMessage());
                tvErrorBanner.setVisibility(View.VISIBLE);
            }
        });
    }
}
