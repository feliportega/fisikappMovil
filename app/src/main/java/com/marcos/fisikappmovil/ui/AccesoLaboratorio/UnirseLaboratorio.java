package com.marcos.fisikappmovil.ui.AccesoLaboratorio;

import android.content.Intent;
import android.os.Bundle;
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

                // VALIDAR VACÍO
                if (codigo.isEmpty()) {
                    edit_unirse.setError("Ingrese un código");
                    return;
                }
                // VALIDAR FORMATO
                if (!codigo.matches("^[A-Z]{3}-\\d{4}$")) {
                    edit_unirse.setError("Formato inválido. Ej: LAB-2024");
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

        Call<UnirLaboratorio> call =
                api.getUnirlaboratorio(codigo_lab);

        call.enqueue(new Callback<UnirLaboratorio>() {

            @Override
            public void onResponse(Call<UnirLaboratorio> call,
                                   Response<UnirLaboratorio> response) {

                if (response.isSuccessful() && response.body() != null) {

                    UnirLaboratorio unirLaboratorio = response.body();

                    Toast.makeText(
                            UnirseLaboratorio.this,
                            "Laboratorio encontrado",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            UnirseLaboratorio.this,
                            ViewsLaboratorio.class
                    );

                    // ENVIAR DATOS
                    intent.putExtra("id_lab", unirLaboratorio.getId());
                    intent.putExtra("codigo_lab",
                            unirLaboratorio.getCodigo_lab());

                    startActivity(intent);

                } else {

                    Toast.makeText(
                            UnirseLaboratorio.this,
                            "Código inválido o laboratorio no existe",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<UnirLaboratorio> call,
                                  Throwable throwable) {

                Toast.makeText(
                        UnirseLaboratorio.this,
                        "Error de conexión: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}