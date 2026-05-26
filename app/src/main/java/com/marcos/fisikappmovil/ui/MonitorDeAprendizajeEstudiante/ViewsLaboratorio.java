package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.models.Laboratorio;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewsLaboratorio extends AppCompatActivity {

    TextView txtTituloLab, txtResumenLab;

    Button btnPractica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views_laboratorio);

        // =========================
        // COMPONENTES
        // =========================

        txtTituloLab = findViewById(R.id.txtTituloLab);
        txtResumenLab = findViewById(R.id.txtResumenLab);

        btnPractica = findViewById(R.id.btnPractica);

        // =========================
        // BOTÓN COMENZAR LABORATORIO
        // =========================

        btnPractica.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ViewsLaboratorio.this,
                    PasosLaboratorio.class
            );

            startActivity(intent);
        });

        // =========================
        // CONSUMO DEL BACKEND
        // =========================

        FisikappApi api = RetrofitClient
                .getClient()
                .create(FisikappApi.class);

        api.getLaboratorio(8).enqueue(new Callback<Laboratorio>() {

            @Override
            public void onResponse(Call<Laboratorio> call,
                                   Response<Laboratorio> response) {

                if (response.isSuccessful() && response.body() != null) {

                    Laboratorio laboratorio = response.body();

                    txtTituloLab.setText(
                            laboratorio.getTitulo_lab()
                    );

                    txtResumenLab.setText(
                            laboratorio.getResumen()
                    );
                }
            }

            @Override
            public void onFailure(Call<Laboratorio> call, Throwable t) {

                txtTituloLab.setText("Error de conexión");

                txtResumenLab.setText(
                        t.getMessage()
                );
            }
        });
    }
}
