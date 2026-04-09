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

public class Login extends AppCompatActivity {
    Button btnregistro;
    Button btnrecuperar;
    Button btnsesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        btnregistro = findViewById(R.id.btnResgistrarse);
        btnrecuperar = findViewById(R.id.btnRecuperarc);
        btnsesion = findViewById(R.id.btnIniciarsesion);

        btnregistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irReg = new Intent(Login.this,Register.class);
                startActivity(irReg);
            }
        });

        btnrecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irRecuperar = new Intent(Login.this, RecuperarCuenta.class);
                startActivity(irRecuperar);
            }
        });

        btnsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irDash = new Intent(Login.this, Dashboard.class);
                startActivity(irDash);
            }
        });

    }

}