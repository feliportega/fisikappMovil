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

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.models.Laboratorio;
import com.marcos.fisikappmovil.ui.AccesoLaboratorio.UnirseLaboratorio;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class Dashboard extends AppCompatActivity {

    Button btemp;

    TextView txtLaboratorio; // Declarada en tu código original

    FisikappApi api;
    RecyclerView recyclerView;
    ImageView imgcerrar_sesion;

    private ImageView imgLaboratorio;
    private TextView txtlaboratorio; // Declarada en tu código original (minúscula)
    private RecyclerView tarjeta;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_dashboard);

        // Inicialización respetando tus nombres
        imgcerrar_sesion = findViewById(R.id.imgcerrar_sesion);
        imgLaboratorio = findViewById(R.id.imgLaboratorio);
        txtLaboratorio = findViewById(R.id.txtLaboratorio); // El TextView con ID camelCase
        txtlaboratorio = findViewById(R.id.txtLaboratorio); // Asignamos el mismo ID a la variable en minúscula
        tarjeta = findViewById(R.id.tarjeta);

        // conexion del RecyclerView del Xml
        recyclerView = findViewById(R.id.tarjeta);

        // como se muestra
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // BOTON
        btemp = findViewById(R.id.btemp);

        // RETROFIT
        api = RetrofitClient
                .getClient()
                .create(FisikappApi.class);

        // CONSUMIR API
        cargarLaboratorio();


        // CLICK BOTON
        btemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iremp = new Intent(Dashboard.this, UnirseLaboratorio.class);
                startActivity(iremp);
            }
        });
        imgcerrar_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iremp = new Intent(Dashboard.this, Popup_perfil.class);
                startActivity(iremp);
            }
        });
    }

    private void cargarLaboratorio() {

        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzc4MzUwODA4LCJpYXQiOjE3NzgzNDM2MDgsImp0aSI6IjI3YmZmOGE0ZDJmZTQ4NjNhMDA3NTMzMGQ5ZGQzMjFiIiwidXNlcl9pZCI6IjIifQ.alhBSqJq_j0RSOGb5_uqNsBfoCay25AuM27xK7egExY";

        api.getLaboratorios(token)
                .enqueue(new Callback<List<Laboratorio>>() {

                    @Override
                    public void onResponse(Call<List<Laboratorio>> call, Response<List<Laboratorio>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Laboratorio> lista = response.body();

                            LaboratorioAdapter adapter = new LaboratorioAdapter(lista);
                            recyclerView.setAdapter(adapter);

                            // Ejecutamos la lógica de visibilidad con la lista recibida
                            actualizarVistaLaboratorio(lista);
                        } else {
                            // Si la respuesta es vacía o falla, mostramos el estado inicial
                            actualizarVistaLaboratorio(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Laboratorio>> call, Throwable throwable) {
                        Log.e("API_ERROR", throwable.getMessage());
                        // En caso de error de red, mostramos el estado inicial (copa visible)
                        actualizarVistaLaboratorio(null);
                    }
                });
    }

    private void actualizarVistaLaboratorio(List<?> listaLaboratorio) {
        if (listaLaboratorio != null && !listaLaboratorio.isEmpty()) {
            // SI HAY LABORATORIOS: Se ocultan los elementos de bienvenida
            imgLaboratorio.setVisibility(View.GONE);
            if (txtlaboratorio != null) txtlaboratorio.setVisibility(View.GONE);
            if (txtLaboratorio != null) txtLaboratorio.setVisibility(View.GONE);

            tarjeta.setVisibility(View.VISIBLE);
        } else {
            // CUANDO NO HAY LABORATORIOS: Muestra la copa y el título
            imgLaboratorio.setVisibility(View.VISIBLE);
            if (txtlaboratorio != null) txtlaboratorio.setVisibility(View.VISIBLE);
            if (txtLaboratorio != null) txtLaboratorio.setVisibility(View.VISIBLE);

            tarjeta.setVisibility(View.GONE);
        }
    }
}