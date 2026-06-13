package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.google.gson.JsonObject;
import com.marcos.fisikappmovil.models.MenuActivity;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewsLaboratorio extends MenuActivity {
    private int idLaboratorioRecibido = -1;

    TextView txtTituloLab, txtResumenLab, tvConfig;
    ImageButton btnRdash;
    FisikappApi fisikappApi;
    TokenManager tokenManager; // <-- Declarada correctamente aquí arriba

    Button btnPractica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views_laboratorio);
        configurarMenu();

        // COMPONENTES
        // =========================
        tvConfig = findViewById(R.id.tvConfig);
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

        // INICIALIZACIONES
        fisikappApi = RetrofitClient.getClient().create(FisikappApi.class);
        tokenManager = new TokenManager(this);

        // RECUPERAR ID DINÁMICO
        idLaboratorioRecibido = getIntent().getIntExtra("LABORATORIO_ID", -1);
        int idInscripcionRecibida = getIntent().getIntExtra("INSCRIPCION_ID", -1);

        // Control de seguridad
        if (idLaboratorioRecibido == -1) {
            Log.e("VIEWS_LAB_API", "Error: No se recibió un LABORATORIO_ID válido.");
            if (txtTituloLab != null) txtTituloLab.setText("Error al abrir el laboratorio");
            return;
        }

        // BOTÓN COMENZAR LABORATORIO
        btnPractica.setOnClickListener(v -> {
            Intent intent = new Intent(ViewsLaboratorio.this, PasosLaboratorio.class);
            intent.putExtra("LABORATORIO_ID", idLaboratorioRecibido);
            startActivity(intent);
        });

        // LLAMADA A LA API (¡Ya usa el ID dinámico que pasamos desde el adapter!)
        cargarLaboratorio(idLaboratorioRecibido);
    }

    // CONSUMO DEL BACKEND
    private void cargarLaboratorio(int id) {
        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token no encontrado", Toast.LENGTH_SHORT).show();
            if (txtTituloLab != null) txtTituloLab.setText("Error de autenticación");
            return;
        }

        String authHeader = "Bearer " + token;

        fisikappApi.getLaboratorioPorId(authHeader, id)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject labJson = response.body();

                            String titulo = labJson.has("titulo_lab") && !labJson.get("titulo_lab").isJsonNull()
                                    ? labJson.get("titulo_lab").getAsString()
                                    : "Sin título";

                            String resumen = labJson.has("resumen") && !labJson.get("resumen").isJsonNull()
                                    ? labJson.get("resumen").getAsString()
                                    : "Sin descripción disponible";

                            if (txtTituloLab != null) txtTituloLab.setText(titulo);
                            if (txtResumenLab != null) txtResumenLab.setText(resumen);

                        } else {
                            Log.e("VIEWS_LAB_API", "Error en respuesta: " + response.code());
                            if (txtTituloLab != null) txtTituloLab.setText("Error al cargar detalles (Código: " + response.code() + ")");
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