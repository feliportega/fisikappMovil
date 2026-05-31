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

/**
 * Activity del Dashboard principal.
 * Muestra la bienvenida personalizada al usuario y la lista de laboratorios disponibles.
 */
public class Dashboard extends AppCompatActivity {

    Button btemp;
    TextView txtLaboratorio; // Mensaje de bienvenida / estado
    TextView tvNombreBarra;  // Nombre en la barra superior

    FisikappApi api;
    RecyclerView recyclerView;
    ImageView imgcerrar_sesion;
    private ImageView imgLaboratorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Inicialización limpia de componentes
        imgcerrar_sesion = findViewById(R.id.imgcerrar_sesion);
        imgLaboratorio = findViewById(R.id.imgLaboratorio);
        txtLaboratorio = findViewById(R.id.txtLaboratorio);
        tvNombreBarra = findViewById(R.id.tvNombreUsuarioBarra);

        // Configuración del RecyclerView
        recyclerView = findViewById(R.id.tarjeta);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Optimización para que el RecyclerView calcule bien los tamaños dentro de un ScrollView
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);

        btemp = findViewById(R.id.btemp);
        api = RetrofitClient.getClient().create(FisikappApi.class);

        // Recibir y mostrar el nombre del usuario de forma segura
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

    /**
     * Obtiene la lista de laboratorios desde la API.
     */
    private void cargarLaboratorio() {
        TokenManager tokenManager = new TokenManager(this);
        String tokenGuardado = tokenManager.getToken();

        if (tokenGuardado == null || tokenGuardado.isEmpty()) {
            Log.e("TOKEN", "No existe token");
            return;
        }

        String token = "Bearer " + tokenGuardado;

        api.getMisLaboratorios(token).enqueue(new Callback<List<Incripcion>>() {
            @Override
            public void onResponse(Call<List<Incripcion>> call, Response<List<Incripcion>> response) {
                Log.d("CODIGO", String.valueOf(response.code()));

                List<Incripcion> lista = null;

                if (response.body() != null) {
                    Log.d("CANTIDAD", String.valueOf(response.body().size()));
                }

                if (response.isSuccessful() && response.body() != null) {
                    lista = response.body();
                    Log.d("LABS_ENCONTRADOS", String.valueOf(lista.size()));

                    // Ahora le enviamos también la variable 'token' que armaste arriba
                    LaboratorioAdapter adapter = new LaboratorioAdapter(lista);
                    recyclerView.setAdapter(adapter);

                    actualizarVistaLaboratorio(lista);
                } else {
                    actualizarVistaLaboratorio(null);
                }
            }

            @Override
            public void onFailure(Call<List<Incripcion>> call, Throwable throwable) {
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
            // SI HAY DATOS: Ocultamos la imagen de marcador de posición (tubo)
            if (imgLaboratorio != null) imgLaboratorio.setVisibility(View.GONE);

            // Mantenemos el texto de bienvenida visible y mostramos la lista
            if (txtLaboratorio != null) txtLaboratorio.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        } else {
            // NO HAY DATOS o Error: Mostramos los elementos por defecto de pantalla vacía
            if (imgLaboratorio != null) imgLaboratorio.setVisibility(View.VISIBLE);
            if (txtLaboratorio != null) {
                txtLaboratorio.setVisibility(View.VISIBLE);
                txtLaboratorio.setText("No hay laboratorios disponibles hoy");
            }
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        }
    }
}