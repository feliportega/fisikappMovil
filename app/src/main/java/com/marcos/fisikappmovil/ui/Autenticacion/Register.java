package com.marcos.fisikappmovil.ui.Autenticacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    EditText edtNombre, edtCorreo, edtPassword, edtConfirmar;
    Button btnCrearCuenta;
    TextView btnYaTienesCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        edtNombre = findViewById(R.id.edtNombrecompleto);
        edtCorreo = findViewById(R.id.editTextText); // ID del XML
        edtPassword = findViewById(R.id.editText); // ID del XML
        edtConfirmar = findViewById(R.id.edtConfirmarcont);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        btnYaTienesCuenta = findViewById(R.id.btnYaTienesCuenta);

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

    private void validarYRegistrar() {
        String nombre = edtNombre.getText().toString().trim();
        String correo = edtCorreo.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmar = edtConfirmar.getText().toString().trim();

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmar)) {
            edtConfirmar.setError("Las contraseñas no coinciden");
            return;
        }

        if (password.length() < 8) {
            edtPassword.setError("La contraseña debe tener al menos 8 caracteres");
            return;
        }

        realizarRegistro(nombre, correo, password);
    }

    private void realizarRegistro(String nombre, String correo, String password) {
        RegisterRequest request = new RegisterRequest(nombre, correo, password);
        FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);
        Call<Void> call = api.register(request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Register.this, "Registro exitoso. ¡Ya puedes iniciar sesión!", Toast.LENGTH_LONG).show();
                    finish(); // Cierra registro y vuelve al login
                } else {
                    Toast.makeText(Register.this, "Error en el registro. Es posible que el correo ya esté en uso.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Register.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
