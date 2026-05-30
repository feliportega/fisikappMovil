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
        // RECUPERAR ID DINÁMICO
        // =========================
        // Capturamos el ID del laboratorio que nos mandó el adaptador.
        // Si no encuentra nada, por defecto pondrá -1.
        int idLaboratorioRecibido = getIntent().getIntExtra("LABORATORIO_ID", -1);

        // Si necesitas también el id de la inscripción en el futuro, lo recuperas así:
        int idInscripcionRecibida = getIntent().getIntExtra("INSCRIPCION_ID", -1);

        // Control de seguridad: Si no llegó un ID válido, no hacemos la petición a la API
        if (idLaboratorioRecibido == -1) {
            Log.e("VIEWS_LAB_API", "Error: No se recibió un LABORATORIO_ID válido.");
            if (txtTituloLab != null) txtTituloLab.setText("Error al abrir el laboratorio");
            return;
        }

        // =========================
        // BOTÓN COMENZAR LABORATORIO
        // =========================
        btnPractica.setOnClickListener(v -> {
            Intent intent = new Intent(ViewsLaboratorio.this, PasosLaboratorio.class);
            // Opcional: Si PasosLaboratorio también necesita saber qué laboratorio es, se lo reajustamos:
            intent.putExtra("LABORATORIO_ID", idLaboratorioRecibido);
            startActivity(intent);
        });

        // =========================
        // CONSUMO DEL BACKEND (DINÁMICO)
        // =========================
        FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);

        // 1. Primero obtenemos el token de forma segura usando tu TokenManager
        com.marcos.fisikappmovil.model.TokenManager tokenManager = new com.marcos.fisikappmovil.model.TokenManager(this);
        String tokenGuardado = tokenManager.getToken();
        String token = "Bearer " + tokenGuardado;

        // 2. Pasamos el token y el ID dinámico recuperado (en lugar del 8 estático)
        api.getLaboratorioPorId(token, idLaboratorioRecibido).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject labJson = response.body();

                    // 3. Extraemos las llaves reales de tu tabla Laboratorio en Django
                    // Si en tu base de datos de Django los campos se llaman diferente, ajústalos aquí
                    String titulo = labJson.has("titulo_lab") && !labJson.get("titulo_lab").isJsonNull() ? labJson.get("titulo_lab").getAsString() : "Sin título";
                    String resumen = labJson.has("resumen") && !labJson.get("resumen").isJsonNull() ? labJson.get("resumen").getAsString() : "Sin descripción disponible";

                    // 4. Pintamos las respuestas en tus TextViews de la interfaz
                    if (txtTituloLab != null) txtTituloLab.setText(titulo);
                    if (txtResumenLab != null) txtResumenLab.setText(resumen);

                } else {
                    Log.e("VIEWS_LAB_API", "Error en respuesta: " + response.code());
                    if (txtTituloLab != null) txtTituloLab.setText("Error al cargar detalles");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("VIEWS_LAB_API", "Error de red: " + t.getMessage());
                if (txtTituloLab != null) txtTituloLab.setText("Sin conexión a internet");
            }
        });
    }
}