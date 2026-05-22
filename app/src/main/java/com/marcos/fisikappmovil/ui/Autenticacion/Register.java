package com.marcos.fisikappmovil.ui.Autenticacion;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import com.marcos.fisikappmovil.remote.request.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity {

    EditText edtNombre, edtIdentificacion, edtFechaNacimiento, edtInstitucion, edtCorreo, edtPassword, edtConfirmar;
    TextView tvErrorBanner;
    ImageView ivShowPasswordReg, ivShowConfirmPassword;
    Button btnCrearCuenta;
    TextView btnYaTienesCuenta;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        edtNombre = findViewById(R.id.edtNombrecompleto);
        edtIdentificacion = findViewById(R.id.edtIdentificacion);
        edtFechaNacimiento = findViewById(R.id.edtFechaNacimiento);
        edtInstitucion = findViewById(R.id.edtInstitucion);
        edtCorreo = findViewById(R.id.editTextText); 
        edtPassword = findViewById(R.id.editText); 
        edtConfirmar = findViewById(R.id.edtConfirmarcont);
        tvErrorBanner = findViewById(R.id.tvErrorBanner);
        ivShowPasswordReg = findViewById(R.id.ivShowPasswordReg);
        ivShowConfirmPassword = findViewById(R.id.ivShowConfirmPassword);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        btnYaTienesCuenta = findViewById(R.id.btnYaTienesCuenta);

        ivShowPasswordReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        ivShowConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleConfirmPasswordVisibility();
            }
        });

        btnCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarYRegistrar();
            }
        });

        btnYaTienesCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Regresa al Login
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivShowPasswordReg.setImageResource(R.drawable.baseline_remove_red_eye_24);
        } else {
            edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        isPasswordVisible = !isPasswordVisible;
        edtPassword.setSelection(edtPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            edtConfirmar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivShowConfirmPassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
        } else {
            edtConfirmar.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        edtConfirmar.setSelection(edtConfirmar.getText().length());
    }

    private void validarYRegistrar() {
        String nombre = edtNombre.getText().toString().trim();
        String identificacion = edtIdentificacion.getText().toString().trim();
        String fechaNacimiento = edtFechaNacimiento.getText().toString().trim();
        String institucion = edtInstitucion.getText().toString().trim();
        String correo = edtCorreo.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmar = edtConfirmar.getText().toString().trim();

        tvErrorBanner.setVisibility(View.GONE);

        if (nombre.isEmpty() || identificacion.isEmpty() || fechaNacimiento.isEmpty() || 
            institucion.isEmpty() || correo.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            tvErrorBanner.setText("Please complete all fields");
            tvErrorBanner.setVisibility(View.VISIBLE);
            return;
        }

        if (!password.equals(confirmar)) {
            tvErrorBanner.setText("Passwords do not match");
            tvErrorBanner.setVisibility(View.VISIBLE);
            return;
        }

        if (password.length() < 8) {
            tvErrorBanner.setText("Password must be at least 8 characters");
            tvErrorBanner.setVisibility(View.VISIBLE);
            return;
        }

        realizarRegistro(nombre, identificacion, fechaNacimiento, institucion, correo, password);
    }

    private void realizarRegistro(String nombre, String identificacion, String fechaNacimiento, String institucion, String correo, String password) {
        RegisterRequest request = new RegisterRequest(nombre, identificacion, fechaNacimiento, institucion, correo, password);
        FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);
        Call<Void> call = api.register(request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Register.this, "Registration successful. You can now log in!", Toast.LENGTH_LONG).show();
                    finish(); 
                } else {
                    tvErrorBanner.setText("Registration failed. Email might already be in use.");
                    tvErrorBanner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                tvErrorBanner.setText("Network error: " + t.getMessage());
                tvErrorBanner.setVisibility(View.VISIBLE);
            }
        });
    }
}
