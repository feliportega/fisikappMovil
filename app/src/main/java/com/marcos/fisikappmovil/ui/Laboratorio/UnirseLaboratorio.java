package com.marcos.fisikappmovil.ui.Laboratorio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.ViewsLaboratorio;

public class UnirseLaboratorio extends AppCompatActivity {

    Button btnuni;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unirse_laboratorio);

        btnuni=findViewById(R.id.btnUnirse);
        btnuni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inta = new Intent(UnirseLaboratorio.this, ViewsLaboratorio.class);
                startActivity(inta);
            }
        });
    }
}