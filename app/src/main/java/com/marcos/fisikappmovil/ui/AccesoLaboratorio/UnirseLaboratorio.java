package com.marcos.fisikappmovil.ui.AccesoLaboratorio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.models.UnirLaboratorio;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.ViewsLaboratorio;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnirseLaboratorio extends AppCompatActivity {

    EditText edit_unirse;
    TokenManager tokenManager;
    Button btnUnirse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unirse_laboratorio);

        edit_unirse = findViewById(R.id.edit_unirse);
        btnUnirse = findViewById(R.id.btnUnirse);
        tokenManager = new TokenManager(this);

        btnUnirse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String codigo = edit_unirse.getText().toString().trim();

                if (codigo.isEmpty()) {
                    edit_unirse.setError("Ingrese un código");
                    return;
                }

                buscarLaboratorio(codigo);
            }
        });
    }

    private void buscarLaboratorio(String codigo_lab) {

        FisikappApi api = RetrofitClient
                .getClient()
                .create(FisikappApi.class);

        // BODY
        UnirLaboratorio unirLaboratorio =
                new UnirLaboratorio(codigo_lab);

        String tokenGuardado =
                tokenManager.getToken();

        String token =
                "Bearer " + tokenGuardado;

        Call<JsonObject> call =
                api.postUnirlaboratorio(token, unirLaboratorio);

        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call,
                                   Response<JsonObject> response) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    JsonObject json = response.body();

                    String mensaje =
                            json.get("mensaje").getAsString();

                    int idLaboratorio =
                            json.getAsJsonObject("laboratorio")
                                    .get("id")
                                    .getAsInt();

                    Toast.makeText(
                            UnirseLaboratorio.this,
                            mensaje,
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            UnirseLaboratorio.this,
                            ViewsLaboratorio.class
                    );

                    intent.putExtra(
                            "id_lab",
                            idLaboratorio
                    );

                    startActivity(intent);

                } else {

                    try {

                        String error =
                                response.errorBody().string();

                        Log.e("ERROR_API", error);

                        Toast.makeText(
                                UnirseLaboratorio.this,
                                "Error: " + error,
                                Toast.LENGTH_LONG
                        ).show();

                    } catch (Exception e) {

                        e.printStackTrace();

                        Toast.makeText(
                                UnirseLaboratorio.this,
                                "Error: " + response.code(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call,
                                  Throwable throwable) {

                Log.e("ERROR_RETROFIT",
                        throwable.getMessage());

                Toast.makeText(
                        UnirseLaboratorio.this,
                        "Error: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}