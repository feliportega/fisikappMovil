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

        // Usamos el método corregido getLaboratorioPorId que creamos en la interfaz
        api.getLaboratorioPorId(8).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject laboratorioJson = response.body();

                    // Extraemos los textos de forma segura validando si las llaves existen en tu backend de Django
                    String titulo = laboratorioJson.has("titulo_lab") && !laboratorioJson.get("titulo_lab").isJsonNull()
                            ? laboratorioJson.get("titulo_lab").getAsString() : "Sin título disponible";

                    String resumen = laboratorioJson.has("resumen") && !laboratorioJson.get("resumen").isJsonNull()
                            ? laboratorioJson.get("resumen").getAsString() : "Sin resumen disponible";

                    // Seteamos los textos reales en los TextViews
                    if (txtTituloLab != null) txtTituloLab.setText(titulo);
                    if (txtResumenLab != null) txtResumenLab.setText(resumen);
                } else {
                    if (txtTituloLab != null) txtTituloLab.setText("Error al obtener el laboratorio");
                    Log.e("VIEWS_LAB_API", "Código de error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (txtTituloLab != null) txtTituloLab.setText("Error de conexión");
                if (txtResumenLab != null) txtResumenLab.setText(t.getMessage());
                Log.e("VIEWS_LAB_API", "Error: " + t.getMessage());
            }
        });
    }
}