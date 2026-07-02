package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.models.MenuActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConceptosBasicos extends MenuActivity {

    private int idLaboratorio = -1;

    private TextView txtTituloLaboratorio;
    private TextView txtResumen;
    private TextView txtPrologo;
    private TextView txtIntroduccion;
    private TextView txtMarcoTeorico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conceptos_basicos);

        configurarMenu();

        // Obtiene el laboratorio seleccionado desde la pantalla anterior
        idLaboratorio = getIntent().getIntExtra("LABORATORIO_ID", -1);

        inicializarVistas();
        configurarBotonRegresar();
        configurarBotonFormulas();

        Log.d("CONCEPTOS_API", "ID recibido: " + idLaboratorio);

        if (idLaboratorio != -1) {
            cargarLaboratorio();
        }
    }

    private void inicializarVistas() {
        txtTituloLaboratorio = findViewById(R.id.txtTituloLaboratorio);
        txtResumen = findViewById(R.id.txtResumen);
        txtPrologo = findViewById(R.id.txtPrologo);
        txtIntroduccion = findViewById(R.id.txtIntroduccion);
        txtMarcoTeorico = findViewById(R.id.txtMarcoTeorico);
    }

    private void configurarBotonRegresar() {
        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    // Navega a la siguiente pantalla del flujo del laboratorio
    private void configurarBotonFormulas() {
        TextView btnVerFormulas = findViewById(R.id.btnFormulas);

        if (btnVerFormulas != null) {
            btnVerFormulas.setOnClickListener(v -> {
                Intent intent = new Intent(
                        ConceptosBasicos.this,
                        Laboratorio_experimental.class
                );
                startActivity(intent);
            });
        }
    }

    // Consulta la información del laboratorio y la muestra en pantalla
    private void cargarLaboratorio() {

        FisikappApi api = RetrofitClient
                .getClient()
                .create(FisikappApi.class);

        TokenManager tokenManager = new TokenManager(this);
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

                            JsonObject laboratorio = response.body();

                            Log.d(
                                    "CONCEPTOS_API",
                                    laboratorio.toString()
                            );

                            String titulo =
                                    laboratorio.has("titulo_lab")
                                            && !laboratorio.get("titulo_lab").isJsonNull()
                                            ? laboratorio.get("titulo_lab").getAsString()
                                            : "Sin título";

                            String resumen =
                                    laboratorio.has("resumen")
                                            && !laboratorio.get("resumen").isJsonNull()
                                            ? laboratorio.get("resumen").getAsString()
                                            : "Sin resumen";

                            String prologo =
                                    laboratorio.has("prologo")
                                            && !laboratorio.get("prologo").isJsonNull()
                                            ? laboratorio.get("prologo").getAsString()
                                            : "Sin prólogo";

                            String introduccion =
                                    laboratorio.has("introduccion")
                                            && !laboratorio.get("introduccion").isJsonNull()
                                            ? laboratorio.get("introduccion").getAsString()
                                            : "Sin introducción";

                            String marcoTeorico =
                                    laboratorio.has("marco_teorico")
                                            && !laboratorio.get("marco_teorico").isJsonNull()
                                            ? laboratorio.get("marco_teorico").getAsString()
                                            : "No existe marco teórico";

                            txtTituloLaboratorio.setText(titulo);
                            txtResumen.setText(resumen);
                            txtPrologo.setText(prologo);
                            txtIntroduccion.setText(introduccion);
                            txtMarcoTeorico.setText(marcoTeorico);

                        } else {

                            Log.e(
                                    "CONCEPTOS_API",
                                    "Código respuesta: " + response.code()
                            );

                            txtMarcoTeorico.setText(
                                    "Error al cargar el contenido"
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
                                "Error: " + t.getMessage()
                        );

                        txtMarcoTeorico.setText(
                                "Error de conexión"
                        );
                    }
                });
    }
}