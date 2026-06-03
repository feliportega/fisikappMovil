package com.marcos.fisikappmovil.models;

import android.content.Intent;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;
import com.marcos.fisikappmovil.ui.GestionDePerfilDelEstudiante.Perfil_del_estudiante;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.InLaboratorio;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.InformeLaboratorio;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.ViewsLaboratorio;

public class MenuActivity extends AppCompatActivity {
    protected void configurarMenu() {

        Log.d("MENU", "Entró a configurarMenu");


        LinearLayout btnInicio = findViewById(R.id.btnInicio);
        LinearLayout btnLaboratorios = findViewById(R.id.btnLaboratorios);
        LinearLayout btnInformes = findViewById(R.id.btnInformes);
        LinearLayout btnPerfil = findViewById(R.id.btnPerfil);

        if (btnInicio != null) {
            btnInicio.setOnClickListener(v ->
                    startActivity(new Intent(this, Dashboard.class)));
        }

        if (btnLaboratorios != null) {
            btnLaboratorios.setOnClickListener(v ->
                    startActivity(new Intent(this, ViewsLaboratorio.class)));
        }

        if (btnInformes != null) {
            btnInformes.setOnClickListener(v ->
                    startActivity(new Intent(this, InLaboratorio.class)));
        }

        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(v ->
                    startActivity(new Intent(this, Perfil_del_estudiante.class)));
        }
    }
}
