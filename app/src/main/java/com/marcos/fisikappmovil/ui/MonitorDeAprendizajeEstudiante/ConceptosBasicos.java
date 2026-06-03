package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

import android.util.Log;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConceptosBasicos extends AppCompatActivity {

    private int idLaboratorio = -1;
    private TextView txtMarcoTeorico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conceptos_basicos);
        idLaboratorio = getIntent().getIntExtra("LABORATORIO_ID", -1);

        txtMarcoTeorico = findViewById(R.id.txtMarcoTeorico);
        Log.d("CONCEPTOS_API", "ID recibido: " + idLaboratorio);

        // Botón para regresar
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }


        // =========================
// CONSUMO MARCO TEÓRICO
// =========================

        if (idLaboratorio != -1) {

            FisikappApi api = RetrofitClient
                    .getClient()
                    .create(FisikappApi.class);

            com.marcos.fisikappmovil.model.TokenManager tokenManager =
                    new com.marcos.fisikappmovil.model.TokenManager(this);

            String token = "Bearer " + tokenManager.getToken();

            Log.d("CONCEPTOS_API", "Token: " + token);
            Log.d("CONCEPTOS_API", "ID: " + idLaboratorio);

            api.getLaboratorioPorId(token, idLaboratorio)
                    .enqueue(new Callback<JsonObject>() {

                        @Override
                        public void onResponse(
                                Call<JsonObject> call,
                                Response<JsonObject> response
                        ) {
                            Log.d(
                                    "CONCEPTOS_API",
                                    "Response code: " + response.code()
                            );

                            if (response.isSuccessful()
                                    && response.body() != null) {

                                JsonObject laboratorio =
                                        response.body();
                                Log.d(
                                        "CONCEPTOS_API",
                                        laboratorio.toString()
                                );

                                String marcoTeorico =
                                        laboratorio.has("marco_teorico")
                                                && !laboratorio.get("marco_teorico").isJsonNull()
                                                ? laboratorio.get("marco_teorico").getAsString()
                                                : "No existe marco teórico";

                                txtMarcoTeorico.setText(
                                        marcoTeorico
                                );

                            } else {

                                Log.e(
                                        "CONCEPTOS_API",
                                        "Código respuesta: " + response.code()
                                );

                                txtMarcoTeorico.setText(
                                        "Error al cargar el marco teórico"
                                );
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<JsonObject> call,
                                Throwable t
                        ) {

                            Log.e(
                                    "CONCEPTOS_API",
                                    t.getMessage()
                            );

                            txtMarcoTeorico.setText(
                                    "Error de conexión"
                            );
                        }
                    });
        }
    }

}


