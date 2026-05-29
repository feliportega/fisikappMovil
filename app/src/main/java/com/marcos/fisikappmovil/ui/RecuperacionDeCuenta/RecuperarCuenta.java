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

/**
 * Activity que gestiona el inicio de la recuperación de cuenta.
 * Envía el correo del usuario al servidor para que este genere un enlace
 * de restablecimiento que será procesado en la plataforma web.
 */
public class RecuperarCuenta extends AppCompatActivity {

    TextView btnvolversesion;
    Button btnirrestablecer;
    EditText etEmail;
    TextView tvErrorBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_cuenta);

        btnvolversesion = findViewById(R.id.btnRegresarsesion);
        btnirrestablecer = findViewById(R.id.btnSRestableser);
        etEmail = findViewById(R.id.etEmailRecuperar);
        tvErrorBanner = findViewById(R.id.tvErrorBanner);

        btnvolversesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Volver al Login
                Intent intent = new Intent(RecuperarCuenta.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        btnirrestablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = etEmail.getText().toString().trim();
                tvErrorBanner.setVisibility(View.GONE);

                if (correo.isEmpty()) {
                    etEmail.setError("Introduce tu correo");
                    return;
                }
                
                // Iniciamos el proceso llamando a la API
                enviarSolicitudRecuperacion(correo);
            }
        });
    }

    /**
     * Envía una petición al backend para solicitar el enlace de recuperación.
     * El servidor enviará un correo electrónico al usuario con un link a la web.
     * 
     * @param correo El correo electrónico ingresado por el usuario.
     */
    private void enviarSolicitudRecuperacion(String correo) {
        EmailRequest request = new EmailRequest(correo);
        FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);
        Call<Void> call = api.recuperarContrasena(request);

        // Deshabilitar botón para evitar múltiples clics
        btnirrestablecer.setEnabled(false);
        btnirrestablecer.setText("Enviando...");

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnirrestablecer.setEnabled(true);
                btnirrestablecer.setText("Siguiente");

                if (response.isSuccessful()) {
                    // Mensaje de éxito informando al usuario
                    Toast.makeText(RecuperarCuenta.this, 
                        "Enlace enviado. Por favor, revisa tu correo para continuar en la web.", 
                        Toast.LENGTH_LONG).show();
                    
                    // Opcional: Regresar al login después de un momento
                    finish();
                } else {
                    // El servidor no encontró el correo o hubo un error de lógica
                    tvErrorBanner.setText("Error: El correo no está registrado o es inválido.");
                    tvErrorBanner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnirrestablecer.setEnabled(true);
                btnirrestablecer.setText("Siguiente");
                
                tvErrorBanner.setText("Error de red: " + t.getMessage());
                tvErrorBanner.setVisibility(View.VISIBLE);
            }
        });
    }
}
