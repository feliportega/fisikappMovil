package com.marcos.fisikappmovil.ui.RecuperacionDeCuenta;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.ui.Autenticacion.Login;

/**
 * Activity que gestiona la redirección del usuario a la plataforma web
 * para realizar la recuperación de su contraseña.
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
                // Al hacer clic, redirigimos directamente a la web
                abrirWebRecuperacion();
            }
        });
    }

    /**
     * Abre el navegador predeterminado del dispositivo con la URL de recuperación.
     * Reemplace la URL de abajo cuando el equipo de backend/web la proporcione.
     */
    private void abrirWebRecuperacion() {
        // --- TODO: REEMPLAZAR CON LA URL REAL ---
        String urlWeb = "https://fisikapp-web.onrender.com/password-reset"; 
        
        try {
            Intent intentWeb = new Intent(Intent.ACTION_VIEW);
            intentWeb.setData(Uri.parse(urlWeb));
            startActivity(intentWeb);
            
            Toast.makeText(this, "Redirigiendo a la plataforma web...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el navegador: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
