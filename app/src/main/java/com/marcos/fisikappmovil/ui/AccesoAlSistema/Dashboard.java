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

/**
 * Activity del Dashboard principal.
 * Muestra la bienvenida personalizada al usuario y la lista de laboratorios disponibles.
 */
public class Dashboard extends AppCompatActivity {

    Button btemp;
    TextView txtLaboratorio; 
    TextView tvNombreBarra, tvBienvenida; // Nuevas vistas para el nombre dinámico

    FisikappApi api;
    RecyclerView recyclerView;
    ImageView imgcerrar_sesion;

    private ImageView imgLaboratorio;
    private TextView txtlaboratorio; 
    private RecyclerView tarjeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Inicialización de componentes
        imgcerrar_sesion = findViewById(R.id.imgcerrar_sesion);
        imgLaboratorio = findViewById(R.id.imgLaboratorio);
        txtLaboratorio = findViewById(R.id.txtLaboratorio);
        txtlaboratorio = findViewById(R.id.txtLaboratorio);
        tarjeta = findViewById(R.id.tarjeta);
        
        // Vistas del nombre dinámico
        tvNombreBarra = findViewById(R.id.tvNombreUsuarioBarra);
        tvBienvenida = findViewById(R.id.txtLaboratorio); // Usamos el ID existente para el mensaje central

        // Recibir y mostrar el nombre del usuario
        String nombre = getIntent().getStringExtra("USER_NAME");
        if (nombre != null && !nombre.isEmpty()) {
            if (tvNombreBarra != null) tvNombreBarra.setText(nombre.toUpperCase());
            if (txtLaboratorio != null) txtLaboratorio.setText("¡Bienvenido de nuevo, " + nombre + "!");
        }

        // Configuración del RecyclerView
        recyclerView = findViewById(R.id.tarjeta);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btemp = findViewById(R.id.btemp);
        api = RetrofitClient.getClient().create(FisikappApi.class);

        cargarLaboratorio();

        btemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iremp = new Intent(Dashboard.this, UnirseLaboratorio.class);
                startActivity(iremp);
            }
        });
    }

    /**
     * Obtiene la lista de laboratorios desde la API.
     */
    private void cargarLaboratorio() {
        // Nota: El token debería ser manejado de forma dinámica idealmente
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."; 

        api.getLaboratorios(token).enqueue(new Callback<List<Laboratorio>>() {
            @Override
            public void onResponse(Call<List<Laboratorio>> call, Response<List<Laboratorio>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Laboratorio> lista = response.body();
                    LaboratorioAdapter adapter = new LaboratorioAdapter(lista);
                    recyclerView.setAdapter(adapter);
                    actualizarVistaLaboratorio(lista);
                } else {
                    actualizarVistaLaboratorio(null);
                }
            }

            @Override
            public void onFailure(Call<List<Laboratorio>> call, Throwable throwable) {
                Log.e("API_ERROR", throwable.getMessage());
                actualizarVistaLaboratorio(null);
            }
        });
    }

    /**
     * Gestiona la visibilidad de los elementos según si hay laboratorios o no.
     */
    private void actualizarVistaLaboratorio(List<?> listaLaboratorio) {
        if (listaLaboratorio != null && !listaLaboratorio.isEmpty()) {
            imgLaboratorio.setVisibility(View.GONE);
            if (txtlaboratorio != null) txtlaboratorio.setVisibility(View.GONE);
            if (txtLaboratorio != null) txtLaboratorio.setVisibility(View.GONE);
            tarjeta.setVisibility(View.VISIBLE);
        } else {
            imgLaboratorio.setVisibility(View.VISIBLE);
            if (txtlaboratorio != null) txtlaboratorio.setVisibility(View.VISIBLE);
            if (txtLaboratorio != null) txtLaboratorio.setVisibility(View.VISIBLE);
            tarjeta.setVisibility(View.GONE);
        }
    }
}
