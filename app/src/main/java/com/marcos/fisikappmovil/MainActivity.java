package com.marcos.fisikappmovil;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.ui.Autenticacion.Login;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_FisikappMovil);
        super.onCreate(savedInstanceState);

        // Ir directamente al Login
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);

        finish();
    }
}
