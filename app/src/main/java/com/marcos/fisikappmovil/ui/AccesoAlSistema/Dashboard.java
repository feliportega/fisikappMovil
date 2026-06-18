package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.repository.GrupoRepository;
import com.marcos.fisikappmovil.model.GrupoAcademicoItem;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.ui.Autenticacion.Login;
import com.marcos.fisikappmovil.ui.GestionDePerfilDelEstudiante.Perfil_del_estudiante;

import java.util.List;

public class Dashboard extends AppCompatActivity {

    private TextView txtTituloDashboard;
    private TextView tvNombreBarra;
    private TextView tvSubtituloDashboard;
    private TextView tvEstadoVacio;

    private RecyclerView recyclerView;
    private ImageView imgCerrarSesion;
    private Button btnUnirmeGrupo;

    private TokenManager tokenManager;
    private GrupoRepository grupoRepository;
    private ActivityResultLauncher<Intent> joinGroupLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        initDependencies();
        initViews();
        initLaunchers();
        initListeners();
        setupUserInfo();
        cargarGrupos();
    }

    private void initDependencies() {
        tokenManager = new TokenManager(this);
        grupoRepository = new GrupoRepository();
    }

    private void initLaunchers() {
        joinGroupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        cargarGrupos();
                    }
                }
        );
    }

    private void initViews() {
        imgCerrarSesion = findViewById(R.id.imgcerrar_sesion);

        txtTituloDashboard = findViewById(R.id.txtLaboratorio);
        tvNombreBarra = findViewById(R.id.tvNombreUsuarioBarra);
        tvSubtituloDashboard = findViewById(R.id.tvSubtituloDashboard);
        tvEstadoVacio = findViewById(R.id.tvEstadoVacio);

        recyclerView = findViewById(R.id.tarjeta);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);

        btnUnirmeGrupo = findViewById(R.id.btemp);
    }

    private void initListeners() {
        //imgCerrarSesion.setOnClickListener(v -> cerrarSesion());


        imgCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Perfil_del_estudiante.class);
            startActivity(intent);
        });

        btnUnirmeGrupo.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, UnirseGrupoActivity.class);
            joinGroupLauncher.launch(intent);
        });
    }

    private void setupUserInfo() {
        String nombre = tokenManager.getUserName();

        if (nombre != null && !nombre.trim().isEmpty()) {
            tvNombreBarra.setText(nombre.toUpperCase());
            txtTituloDashboard.setText("Hola, " + nombre);
        } else {
            tvNombreBarra.setText("ESTUDIANTE");
            txtTituloDashboard.setText("Hola, estudiante");
        }

        tvSubtituloDashboard.setText("Estos son tus grupos y actividades asignadas");
    }

    private void cargarGrupos() {
        mostrarEstadoCargando();

        grupoRepository.getMisGrupos(result -> {
            if (result.isSuccess()) {
                List<GrupoAcademicoItem> grupos = result.getData();
                mostrarGrupos(grupos);
            } else {
                mostrarEstadoVacio("No fue posible cargar tus grupos");
            }
        });
    }

    private void mostrarGrupos(List<GrupoAcademicoItem> grupos) {
        if (grupos == null || grupos.isEmpty()) {
            mostrarEstadoVacio("Aún no estás inscrito en ningún grupo");
            return;
        }

        tvEstadoVacio.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        GrupoAcademicoAdapter adapter = new GrupoAcademicoAdapter(grupos, grupo -> {
            Toast.makeText(
                    Dashboard.this,
                    "Abrir grupo: " + grupo.getNombre(),
                    Toast.LENGTH_SHORT
            ).show();

            // Próximo paso:
            // Intent intent = new Intent(Dashboard.this, GrupoDetalleActivity.class);
            // intent.putExtra("GRUPO_ID", grupo.getId());
            // intent.putExtra("GRUPO_NOMBRE", grupo.getNombre());
            // startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void mostrarEstadoCargando() {
        recyclerView.setVisibility(View.GONE);
        tvEstadoVacio.setVisibility(View.VISIBLE);
        tvEstadoVacio.setText("Cargando tus grupos...");
    }

    private void mostrarEstadoVacio(String mensaje) {
        recyclerView.setVisibility(View.GONE);
        tvEstadoVacio.setVisibility(View.VISIBLE);
        tvEstadoVacio.setText(mensaje);
    }

    private void cerrarSesion() {
        tokenManager.clearSession();

        Intent intent = new Intent(Dashboard.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}