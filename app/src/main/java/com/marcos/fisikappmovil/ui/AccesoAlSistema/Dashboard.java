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
import com.marcos.fisikappmovil.security.FaceVault;
import com.marcos.fisikappmovil.ui.AccesoLaboratorio.UnirseLaboratorio;
import com.marcos.fisikappmovil.ui.Autenticacion.FaceConsentActivity;
import com.marcos.fisikappmovil.ui.faceNet.FaceEnrollActivity;

public class Dashboard extends AppCompatActivity {

    Button btemp;

    Button enrol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        btemp = findViewById(R.id.btemp);
        enrol = findViewById(R.id.btempQ);


        btemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iremp = new Intent(Dashboard.this, UnirseLaboratorio.class);
                startActivity(iremp);
            }
        });

        enrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View view) {
                if (FaceVault.hasEmbedding(Dashboard.this)) {
                    new androidx.appcompat.app.AlertDialog.Builder(Dashboard.this)
                            .setTitle("Rostro ya registrado")
                            .setMessage("Ya existe un rostro guardado en este dispositivo. ¿Deseas reemplazarlo?")
                            .setPositiveButton("Sí, reemplazar", (dialog, which) -> {
                                openEnrollFlow();
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    openEnrollFlow();
                }

            }
            private void openEnrollFlow() {
                Intent intent;
                if (FaceVault.hasConsent(Dashboard.this)) {
                    intent = new Intent(Dashboard.this, FaceEnrollActivity.class);
                } else {
                    intent = new Intent(Dashboard.this, FaceConsentActivity.class);
                }
                startActivity(intent);
            }
        });

    }
}
