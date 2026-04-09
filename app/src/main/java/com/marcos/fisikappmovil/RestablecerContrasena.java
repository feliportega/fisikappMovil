package com.marcos.fisikappmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RestablecerContrasena extends AppCompatActivity {
    Button btnnuevapassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restablecer_contrasena);

        btnnuevapassword = findViewById(R.id.btnNuevacontraseña);

        btnnuevapassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irSesion = new Intent(RestablecerContrasena.this, Login.class);
                startActivity(irSesion);
            }
        });
    }
}