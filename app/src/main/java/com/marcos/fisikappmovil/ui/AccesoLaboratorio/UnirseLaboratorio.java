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
    Button btnUnirse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unirse_laboratorio);

        edit_unirse = findViewById(R.id.edit_unirse);
        btnUnirse = findViewById(R.id.btnUnirse);

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

        // Body de la petición
        UnirLaboratorio unirLaboratorio =
                new UnirLaboratorio(codigo_lab);

        // Recuperar token guardado
        TokenManager tokenManager = new TokenManager(this);

        String tokenGuardado = tokenManager.getToken();

        Log.d("TOKEN_GUARDADO", String.valueOf(tokenGuardado));

        if (tokenGuardado == null || tokenGuardado.isEmpty()) {

            Toast.makeText(
                    UnirseLaboratorio.this,
                    "Laboratorio encontrado",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Header Authorization
        String token = "Bearer " + tokenGuardado;

        Log.d("TOKEN_ENVIADO", token);

        Call<UnirLaboratorio> call =
                api.postUnirlaboratorio(token, unirLaboratorio);

        call.enqueue(new Callback<UnirLaboratorio>() {

            @Override
            public void onResponse(Call<UnirLaboratorio> call,
                                   Response<UnirLaboratorio> response) {

                Log.d("RESPUESTA_CODE", String.valueOf(response.code()));
                Log.d("RESPUESTA_BODY", String.valueOf(response.body()));

                if (response.isSuccessful()
                        && response.body() != null) {

                    UnirLaboratorio laboratorio =
                            response.body();

                    Toast.makeText(
                            UnirseLaboratorio.this,
                            "Laboratorio encontrado",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            UnirseLaboratorio.this,
                            ViewsLaboratorio.class
                    );

                    intent.putExtra(
                            "id_lab",
                            laboratorio.getLaboratorio()
                    );

                    startActivity(intent);

                } else {

                    try {

                        String error = "";

                        if (response.errorBody() != null) {
                            error = response.errorBody().string();
                        }

                        Log.e("ERROR_API", error);

                        Toast.makeText(
                                UnirseLaboratorio.this,
                                "Error: " + error,
                                Toast.LENGTH_LONG
                        ).show();

                    } catch (Exception e) {

                        Log.e(
                                "ERROR_EXCEPTION",
                                e.getMessage(),
                                e
                        );

                        Toast.makeText(
                                UnirseLaboratorio.this,
                                "Error: " + response.code(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UnirLaboratorio> call,
                                  Throwable throwable) {

                Log.e(
                        "ERROR_RETROFIT",
                        throwable.getMessage(),
                        throwable
                );

                Toast.makeText(
                        UnirseLaboratorio.this,
                        "Error: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}