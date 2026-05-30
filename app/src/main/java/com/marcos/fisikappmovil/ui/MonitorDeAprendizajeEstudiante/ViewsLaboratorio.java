package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;

// Importamos JsonObject de Google Gson para manejar la respuesta sin la clase Laboratorio
import com.google.gson.JsonObject;

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
            Intent intent = new Intent(ViewsLaboratorio.this, PasosLaboratorio.class);
            startActivity(intent);
        });

        // =========================
        // CONSUMO DEL BACKEND
        // =========================
        FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);

        /// 1. Primero obtenemos el token de forma segura usando tu TokenManager
        com.marcos.fisikappmovil.model.TokenManager tokenManager = new com.marcos.fisikappmovil.model.TokenManager(this);
        String tokenGuardado = tokenManager.getToken();
        String token = "Bearer " + tokenGuardado;

// 2. Ahora sí, le pasamos el token y el ID del laboratorio (el 8 en tu caso)
        api.getLaboratorioPorId(token, 8).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject labJson = response.body();
                    // Aquí va tu lógica para pintar los datos en la vista de este Activity
                } else {
                    Log.e("VIEWS_LAB_API", "Error en respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("VIEWS_LAB_API", "Error de red: " + t.getMessage());
            }
        });
    }
}