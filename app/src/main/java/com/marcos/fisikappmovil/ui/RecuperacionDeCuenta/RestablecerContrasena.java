package com.marcos.fisikappmovil.ui.RecuperacionDeCuenta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    Button btnRestablecer;
    String userEmail;

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
        btnRestablecer = findViewById(R.id.btnNuevacontraseña);

        btnRestablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarYRestablecer();
            }
        });
    }

    private void validarYRestablecer() {
        String uid = etUid.getText().toString().trim();
        String token = etToken.getText().toString().trim();
        String password = etNuevaPass.getText().toString().trim();
        String confirmacion = etConfirmarPass.getText().toString().trim();

        if (uid.isEmpty() || token.isEmpty() || password.isEmpty() || confirmacion.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            etNuevaPass.setError("Mínimo 8 caracteres");
            return;
        }

        if (!password.equals(confirmacion)) {
            etConfirmarPass.setError("Las contraseñas no coinciden");
            return;
        }

        enviarNuevaContrasena(uid, token, password);
    }

    private void enviarNuevaContrasena(String uid, String token, String password) {
        // El correo lo traemos de la pantalla anterior
        ResetPasswordRequest request = new ResetPasswordRequest(userEmail, uid, token, password);
        
        FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);
        Call<Void> call = api.restablecerContrasena(request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RestablecerContrasena.this, "¡Contraseña actualizada con éxito!", Toast.LENGTH_LONG).show();
                    
                    // Volver al Login para que el usuario entre con su nueva clave
                    Intent intent = new Intent(RestablecerContrasena.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RestablecerContrasena.this, "Error: UID o Token inválidos o expirados", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RestablecerContrasena.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
