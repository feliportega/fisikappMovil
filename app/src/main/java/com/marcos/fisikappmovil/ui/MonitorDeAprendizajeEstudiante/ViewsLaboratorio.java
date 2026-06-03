package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;

// Importamos JsonObject de Google Gson para manejar la respuesta sin la clase Laboratorio
import com.google.gson.JsonObject;
import com.marcos.fisikappmovil.models.MenuActivity;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.models.LabResEstudiante;
import com.marcos.fisikappmovil.models.Laboratorio;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewsLaboratorio extends MenuActivity {

    TextView txtTituloLab, txtResumenLab,tvConfig;
    ImageButton btnRdash;
    FisikappApi fisikappApi;
    TokenManager tokenManager;

    Button btnPractica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views_laboratorio);
        configurarMenu();

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


        // RECUPERAR ID DINÁMICO
        int idLaboratorioRecibido = getIntent().getIntExtra("LABORATORIO_ID", -1);

        // Si necesitas también el id de la inscripción en el futuro, lo recuperas así:
        int idInscripcionRecibida = getIntent().getIntExtra("INSCRIPCION_ID", -1);

        // Control de seguridad: Si no llegó un ID válido, no hacemos la petición a la API
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

        cargarLaboratorio(2);

    }

        // CONSUMO DEL BACKEND (DINÁMICO)
        FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);

        com.marcos.fisikappmovil.model.TokenManager tokenManager = new com.marcos.fisikappmovil.model.TokenManager(this);
        String tokenGuardado = tokenManager.getToken();
        String token = "Bearer " + tokenGuardado;
    private void cargarLaboratorio(int id) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Token no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        api.getLaboratorioPorId(token, idLaboratorioRecibido)
                .enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(
                            Call<JsonObject> call,
                            Response<JsonObject> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            JsonObject labJson = response.body();

                            String titulo =
                                    labJson.has("titulo_lab")
                                            && !labJson.get("titulo_lab").isJsonNull()
                                            ? labJson.get("titulo_lab").getAsString()
                                            : "Sin título";

                            String resumen =
                                    labJson.has("resumen")
                                            && !labJson.get("resumen").isJsonNull()
                                            ? labJson.get("resumen").getAsString()
                                            : "Sin descripción disponible";

                            if (txtTituloLab != null) {
                                txtTituloLab.setText(titulo);
                            }

                            if (txtResumenLab != null) {
                                txtResumenLab.setText(resumen);
                            }

                        } else {

                            Log.e(
                                    "VIEWS_LAB_API",
                                    "Error en respuesta: " + response.code()
                            );

                            if (txtTituloLab != null) {
                                txtTituloLab.setText("Error al cargar detalles");
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<JsonObject> call,
                            Throwable t
                    ) {

                        Log.e(
                                "VIEWS_LAB_API",
                                "Error de red: " + t.getMessage()
                        );

                        if (txtTituloLab != null) {
                            txtTituloLab.setText("Sin conexión a internet");
                        }
                    }
                });
    }

}
