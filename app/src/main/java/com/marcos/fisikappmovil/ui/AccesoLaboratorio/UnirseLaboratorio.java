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

        // BODY
        UnirLaboratorio unirLaboratorio =
                new UnirLaboratorio(codigo_lab);

        // TOKEN
        String tokenGuardado =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzc5NzQ3MjEzLCJpYXQiOjE3Nzk3NDAwMTMsImp0aSI6IjMwMDQ5ZGE2NzlkNjRlYzBiOThmZGM4NTAxZDk0MjQ1IiwidXNlcl9pZCI6IjEwNiJ9.fRR0N2_4qmFmNpUQiz_Zpc1gp2AziWlnJRb7ZuLK8q4";

        // AUTHORIZATION
        String token = "Bearer " + tokenGuardado;

        Call<UnirLaboratorio> call =
                api.postUnirlaboratorio(token, unirLaboratorio);

        call.enqueue(new Callback<UnirLaboratorio>() {

            @Override
            public void onResponse(Call<UnirLaboratorio> call,
                                   Response<UnirLaboratorio> response) {

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
                            laboratorio.getId()
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
            public void onFailure(Call<UnirLaboratorio> call,
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