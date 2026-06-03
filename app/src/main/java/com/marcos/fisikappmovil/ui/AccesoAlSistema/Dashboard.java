package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.models.Incripcion;

import com.marcos.fisikappmovil.ui.AccesoLaboratorio.UnirseLaboratorio;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class Dashboard extends AppCompatActivity {

    Button btemp;
    TextView txtLaboratorio;
    TextView tvNombreBarra;

    FisikappApi api;
    RecyclerView recyclerView;
    ImageView imgcerrar_sesion;
    private ImageView imgLaboratorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);


        imgcerrar_sesion = findViewById(R.id.imgcerrar_sesion);
        imgLaboratorio = findViewById(R.id.imgLaboratorio);
        txtLaboratorio = findViewById(R.id.txtLaboratorio);
        tvNombreBarra = findViewById(R.id.tvNombreUsuarioBarra);


        recyclerView = findViewById(R.id.tarjeta);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);

        btemp = findViewById(R.id.btemp);
        api = RetrofitClient.getClient().create(FisikappApi.class);


        String nombre = getIntent().getStringExtra("USER_NAME");
        if (nombre != null && !nombre.isEmpty()) {
            if (tvNombreBarra != null) tvNombreBarra.setText(nombre.toUpperCase());
            if (txtLaboratorio != null) txtLaboratorio.setText("¡Bienvenido de nuevo, " + nombre + "!");
        } else {
            if (txtLaboratorio != null) txtLaboratorio.setText("¡Bienvenido de nuevo!");
        }

        cargarLaboratorio();

        btemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iremp = new Intent(Dashboard.this, UnirseLaboratorio.class);
                startActivity(iremp);
            }
        });
    }


    private void cargarLaboratorio() {
        TokenManager tokenManager = new TokenManager(this);
        String tokenGuardado = tokenManager.getToken();

        if (tokenGuardado == null || tokenGuardado.isEmpty()) {
            Log.e("TOKEN", "No existe token en las preferencias");
            actualizarVistaLaboratorio(null);
            return;
        }

        String token = "Bearer " + tokenGuardado;

        api.getMisLaboratorios(token).enqueue(new Callback<List<Incripcion>>() {
            @Override
            public void onResponse(Call<List<Incripcion>> call, Response<List<Incripcion>> response) {
                Log.d("CODIGO_RESPUESTA", "Código HTTP: " + response.code());

                // Primero validamos si la respuesta del servidor fue exitosa (Códigos 200-299)
                if (response.isSuccessful() && response.body() != null) {
                    List<Incripcion> lista = response.body();
                    Log.d("LABS_ENCONTRADOS", "Cantidad recibida: " + lista.size());

                    // Configurar el adaptador con la lista segura
                    LaboratorioAdapter adapter = new LaboratorioAdapter(lista);
                    recyclerView.setAdapter(adapter);

                    actualizarVistaLaboratorio(lista);
                } else {
                    Log.e("API_ERROR_SERVER", "El servidor respondió con error o cuerpo vacío.");
                    actualizarVistaLaboratorio(null);
                }
            }

            @Override
            public void onFailure(Call<List<Incripcion>> call, Throwable throwable) {
                Log.e("API_ERROR_CONEXION", "Fallo total de red: " + throwable.getMessage());
                actualizarVistaLaboratorio(null);
            }
        });
    }


    private void actualizarVistaLaboratorio(List<?> listaLaboratorio) {
        if (listaLaboratorio != null && !listaLaboratorio.isEmpty()) {
            if (imgLaboratorio != null) imgLaboratorio.setVisibility(View.GONE);


            if (txtLaboratorio != null) txtLaboratorio.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        } else {

            if (imgLaboratorio != null) imgLaboratorio.setVisibility(View.VISIBLE);
            if (txtLaboratorio != null) {
                txtLaboratorio.setVisibility(View.VISIBLE);
                txtLaboratorio.setText("No hay laboratorios disponibles hoy");
            }
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        }
    }
}