package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.models.LabResEstudiante;
import com.marcos.fisikappmovil.models.Laboratorio;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewsLaboratorio extends AppCompatActivity {

    TextView txtTituloLab, txtResumenLab,tvConfig;
    ImageButton btnRdash;
    FisikappApi fisikappApi;
    TokenManager tokenManager;

    Button btnPractica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views_laboratorio);

        // =========================
        // COMPONENTES
        // =========================
        tvConfig=findViewById(R.id.tvConfig);
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

        fisikappApi = RetrofitClient.getClient().create(FisikappApi.class);
        tokenManager = new TokenManager(this);

        btnPractica = findViewById(R.id.btnPractica);


        cargarLaboratorio(2);

    }


    private void cargarLaboratorio(int id) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Token no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;
        //Call<LabResEstudiante> call = FisikappApi.getLaboratorio(authHeader, id);
        Call<LabResEstudiante> call = fisikappApi.getLaboratorio(authHeader,id);
        call.enqueue(new Callback<LabResEstudiante>() {
            @Override
            public void onResponse(Call<LabResEstudiante> call, Response<LabResEstudiante> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LabResEstudiante lab = response.body();
                    txtTituloLab.setText(lab.getTituloLab());
                    txtResumenLab.setText(lab.getCategoria());
                    tvConfig.setText(lab.getPrologo());
                    // Aquí puedes actualizar otras vistas con la información del laboratorio
                } else {
                    Toast.makeText(ViewsLaboratorio.this, "Error al obtener el laboratorio", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LabResEstudiante> call, Throwable t) {
                Toast.makeText(ViewsLaboratorio.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
