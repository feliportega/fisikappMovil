package com.marcos.fisikappmovil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.models.Informe;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InformeLaboratorio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_laboratorio);

        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout layoutObservaciones = findViewById(R.id.layoutObservaciones);
        LinearLayout layoutConclusiones = findViewById(R.id.layoutConclusiones);
        Button btnEnviar = findViewById(R.id.btnEnviar);

        btnBack.setOnClickListener(v -> finish());

        layoutObservaciones.setOnClickListener(v -> {
            Intent intent = new Intent(InformeLaboratorio.this, segundaPantalla.class);
            startActivity(intent);
        });

        layoutConclusiones.setOnClickListener(v -> {
            Intent intent = new Intent(InformeLaboratorio.this, tercerapantalla.class);
            startActivity(intent);
        });

        btnEnviar.setOnClickListener(v -> {
            Intent intent = new Intent(InformeLaboratorio.this, cuartaPantalla.class);
            startActivity(intent);
        });

        // Llamada de prueba para verificar el consumo de la API
        verificarConexionApi();
    }

    private void verificarConexionApi() {
        FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);
        Call<List<Informe>> call = api.getInformes();

        call.enqueue(new Callback<List<Informe>>() {
            @Override
            public void onResponse(Call<List<Informe>> call, Response<List<Informe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Informe> informes = response.body();
                    Log.d("FISIKAPP_API", "Conexión exitosa. Informes encontrados: " + informes.size());
                    for (Informe i : informes) {
                        Log.d("FISIKAPP_API", "ID: " + i.getMateriales());
                    }
                } else {
                    Log.e("FISIKAPP_API", "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Informe>> call, Throwable t) {
                Log.e("FISIKAPP_API", "Fallo total de conexión: " + t.getMessage());
            }
        });
    }
}