package com.marcos.fisikappmovil.ui.RecuperacionDeCuenta;

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
import com.marcos.fisikappmovil.remote.request.EmailRequest;
import com.marcos.fisikappmovil.ui.Autenticacion.Login;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecuperarCuenta extends AppCompatActivity {

    TextView btnvolversesion;
    Button btnirrestablecer;
    EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_cuenta);

        btnvolversesion = findViewById(R.id.btnRegresarsesion);
        btnirrestablecer = findViewById(R.id.btnSRestableser);
        etEmail = findViewById(R.id.etEmailRecuperar);

        btnvolversesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Volver al Login explícitamente
                Intent intent = new Intent(RecuperarCuenta.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        btnirrestablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = etEmail.getText().toString().trim();
                if (correo.isEmpty()) {
                    etEmail.setError("Introduce tu correo");
                    return;
                }
                enviarSolicitudRecuperacion(correo);
            }
        });
    }

    private void enviarSolicitudRecuperacion(String correo) {
        EmailRequest request = new EmailRequest(correo);
        FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);
        Call<Void> call = api.recuperarContrasena(request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RecuperarCuenta.this, "Se ha enviado un enlace a tu correo", Toast.LENGTH_LONG).show();
                    
                    // Al estar en el mismo paquete com.marcos.fisikappmovil.ui.RecuperacionDeCuenta
                    // ya debería reconocer RestablecerContrasena sin problemas
                    Intent intent = new Intent(RecuperarCuenta.this, RestablecerContrasena.class);
                    intent.putExtra("user_email", correo);
                    startActivity(intent);
                } else {
                    Toast.makeText(RecuperarCuenta.this, "Error: Verifica que el correo sea correcto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RecuperarCuenta.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
