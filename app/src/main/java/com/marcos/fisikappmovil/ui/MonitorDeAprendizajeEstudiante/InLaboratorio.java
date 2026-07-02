package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.models.MenuActivity;

public class InLaboratorio extends MenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_informe_laboratorio_ar);
        configurarMenu();
    }
}