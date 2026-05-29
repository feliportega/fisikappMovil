package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.models.Laboratorio;
import com.marcos.fisikappmovil.ui.AccesoLaboratorio.UnirseLaboratorio;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashboard extends AppCompatActivity {

    private Button btemp;

    private TextView tvNombreBarra;
    private TextView txtBienvenida;

    private ImageView imgcerrar_sesion;

    private LinearLayout layoutBienvenida;

    private RecyclerView recyclerLaboratorios;

    private FisikappApi api;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // INICIALIZAR VISTAS
        imgcerrar_sesion = findViewById(R.id.imgcerrar_sesion);

        tvNombreBarra = findViewById(R.id.tvNombreUsuarioBarra);

        txtBienvenida = findViewById(R.id.txtBienvenida);

        layoutBienvenida = findViewById(R.id.layoutBienvenida);

        recyclerLaboratorios = findViewById(R.id.recyclerLaboratorios);

        btemp = findViewById(R.id.btemp);

        // RECYCLERVIEW
        recyclerLaboratorios.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // API
        api = RetrofitClient.getClient().create(FisikappApi.class);

        // NOMBRE USUARIO
        String nombre = getIntent().getStringExtra("USER_NAME");

        if (nombre != null && !nombre.isEmpty()) {

            tvNombreBarra.setText(nombre.toUpperCase());

            txtBienvenida.setText(
                    "¡Bienvenido de nuevo, " + nombre + "!"
            );
        }

        // CARGAR LABORATORIOS
        cargarLaboratorio();

        // BOTON EMPEZAR
        btemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        Dashboard.this,
                        UnirseLaboratorio.class
                );

                startActivity(intent);
            }
        });
    }

    /**
     * CARGAR LABORATORIOS
     */
    private void cargarLaboratorio() {

        String token = "Bearer TU_TOKEN";

        api.getLaboratorios(token).enqueue(new Callback<List<Laboratorio>>() {

            @Override
            public void onResponse(
                    Call<List<Laboratorio>> call,
                    Response<List<Laboratorio>> response
            ) {

                if (response.isSuccessful() && response.body() != null) {

                    List<Laboratorio> lista = response.body();

                    Log.d("LABS", "Cantidad: " + lista.size());

                    // ADAPTER
                    LaboratorioAdapter adapter =
                            new LaboratorioAdapter(lista);

                    recyclerLaboratorios.setAdapter(adapter);

                    // ACTUALIZAR UI
                    actualizarVistaLaboratorio(lista);

                } else {

                    Log.e("API", "Respuesta vacía");

                    actualizarVistaLaboratorio(null);
                }
            }

            @Override
            public void onFailure(
                    Call<List<Laboratorio>> call,
                    Throwable throwable
            ) {

                Log.e("API_ERROR", throwable.getMessage());

                actualizarVistaLaboratorio(null);
            }
        });
    }

    /**
     * MOSTRAR U OCULTAR CONTENIDO
     */
    private void actualizarVistaLaboratorio(
            List<?> listaLaboratorio
    ) {

        if (listaLaboratorio != null
                && !listaLaboratorio.isEmpty()) {

            // MOSTRAR LISTA
            recyclerLaboratorios.setVisibility(View.VISIBLE);

            // OCULTAR BIENVENIDA
            layoutBienvenida.setVisibility(View.GONE);

        } else {

            // MOSTRAR BIENVENIDA
            layoutBienvenida.setVisibility(View.VISIBLE);

            // OCULTAR LISTA
            recyclerLaboratorios.setVisibility(View.GONE);
        }
    }
}