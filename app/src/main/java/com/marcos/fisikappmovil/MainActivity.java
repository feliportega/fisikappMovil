package com.marcos.fisikappmovil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.ui.Autenticacion.Login;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Como ahora la pantalla es un Splash Screen (solo logo como en Figma),
        // pasamos automáticamente a la pantalla de Login después de 2 segundos.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish(); // Cerramos el Splash para que no se pueda volver atrás
            }
        }, 2000); // 2000 milisegundos = 2 segundos
    }
}
