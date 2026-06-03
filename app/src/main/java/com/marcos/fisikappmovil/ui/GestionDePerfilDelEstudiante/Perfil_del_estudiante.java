package com.marcos.fisikappmovil.ui.GestionDePerfilDelEstudiante;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.models.MenuActivity;

public class Perfil_del_estudiante extends MenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_del_estudiante);
        configurarMenu();

    }
}

