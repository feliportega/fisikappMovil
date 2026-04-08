package com.marcos.fisikappmovil;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Iniciar directamente en InformeLaboratorio para ver tus nuevas pantallas
        Intent intent = new Intent(MainActivity.this, InformeLaboratorio.class);
        startActivity(intent);
        finish(); // Cierra MainActivity para que no aparezca el Hello World
    }
}