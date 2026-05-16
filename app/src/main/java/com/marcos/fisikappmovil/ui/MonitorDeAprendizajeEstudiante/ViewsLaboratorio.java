package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.widget.TextView;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.models.Laboratorio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.marcos.fisikappmovil.R;

public class ViewsLaboratorio extends AppCompatActivity {

    CardView btninforme;
    TextView txtTituloLab, txtResumenLab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views_laboratorio);

        txtTituloLab = findViewById(R.id.txtTituloLab);
        txtResumenLab = findViewById(R.id.txtResumenLab);

        btninforme = findViewById(R.id.btnInforme);

        btninforme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent lyfinal = new Intent(ViewsLaboratorio.this, InformeLaboratorio.class);
                startActivity(lyfinal);
            }
        });

        // Botón Conceptos Básicos
        CardView btnConceptos = findViewById(R.id.btnConceptos);

        if (btnConceptos != null) {

            btnConceptos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewsLaboratorio.this, ConceptosBasicos.class);
                    startActivity(intent);
                }
            });
        }

        // Botón Práctica
        CardView btnPractica = findViewById(R.id.btnPractica);

        if (btnPractica != null) {

            btnPractica.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewsLaboratorio.this, PracticaConceptos.class);
                    startActivity(intent);
                }
            });
        }

        // =========================
        // CONSUMO DEL BACKEND
        // =========================

        FisikappApi api = RetrofitClient
                .getClient()
                .create(FisikappApi.class);

        api.getLaboratorio(8).enqueue(new Callback<Laboratorio>() {

            @Override
            public void onResponse(Call<Laboratorio> call, Response<Laboratorio> response) {

                if(response.isSuccessful() && response.body() != null){

                    Laboratorio laboratorio = response.body();

                    txtTituloLab.setText(laboratorio.getTitulo_lab());
                    txtResumenLab.setText(laboratorio.getResumen());
                }
            }

            @Override
            public void onFailure(Call<Laboratorio> call, Throwable t) {

                txtTituloLab.setText("Error al cargar");
            }
        });
    }
}
