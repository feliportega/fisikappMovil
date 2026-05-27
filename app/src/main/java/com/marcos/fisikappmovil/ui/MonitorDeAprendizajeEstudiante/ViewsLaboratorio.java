package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.models.Laboratorio;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewsLaboratorio extends AppCompatActivity {

    TextView txtTituloLab, txtResumenLab;
    ImageButton btnRdash;

    Button btnPractica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views_laboratorio);

        // =========================
        // COMPONENTES
        // =========================

        btnRdash = findViewById(R.id.btnRdash);
        btnRdash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent irdas = new Intent(ViewsLaboratorio.this, Dashboard.class);
                startActivity(irdas);
            }
        });

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
