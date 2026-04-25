package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.ui.AccesoLaboratorio.UnirseLaboratorio;

public class Dashboard extends AppCompatActivity {

    Button btemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        btemp = findViewById(R.id.btemp);

        btemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iremp = new Intent(Dashboard.this, UnirseLaboratorio.class);
                startActivity(iremp);
            }
        });
    }
}

//SALAZAR--------------