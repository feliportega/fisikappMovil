package com.marcos.fisikappmovil.ui.Laboratorio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.repository.LaboratorioRepository;
import com.marcos.fisikappmovil.model.LaboratorioAsignadoItem;
import com.marcos.fisikappmovil.ui.Laboratorio.DetalleLaboratorioActivity;
import com.marcos.fisikappmovil.model.TokenManager;

import java.util.List;

public class GrupoLaboratoriosActivity extends AppCompatActivity {

    public static final String EXTRA_GRUPO_ID = "GRUPO_ID";
    public static final String EXTRA_GRUPO_NOMBRE = "GRUPO_NOMBRE";
    public static final String EXTRA_GRUPO_CURSO = "GRUPO_CURSO";

    private ImageView btnBackGrupoLabs;
    private TextView tvNombreUsuarioBarraGrupoLabs;
    private TextView tvTituloGrupoLabs;
    private TextView tvSubtituloGrupoLabs;
    private TextView tvResumenGrupoLabs;
    private TextView tvEstadoVacioGrupoLabs;
    private RecyclerView rvLaboratoriosAsignados;

    private TokenManager tokenManager;
    private LaboratorioRepository laboratorioRepository;

    private int grupoId;
    private String grupoNombre;
    private String grupoCurso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grupo_laboratorios);

        initDependencies();
        readExtras();
        initViews();
        initListeners();
        setupHeader();
        cargarLaboratorios();
    }

    private void initDependencies() {
        tokenManager = new TokenManager(this);
        laboratorioRepository = new LaboratorioRepository();
    }

    private void readExtras() {
        Intent intent = getIntent();

        grupoId = intent.getIntExtra(EXTRA_GRUPO_ID, -1);
        grupoNombre = intent.getStringExtra(EXTRA_GRUPO_NOMBRE);
        grupoCurso = intent.getStringExtra(EXTRA_GRUPO_CURSO);

        if (grupoNombre == null || grupoNombre.trim().isEmpty()) {
            grupoNombre = "Grupo académico";
        }

        if (grupoCurso == null || grupoCurso.trim().isEmpty()) {
            grupoCurso = "Laboratorios asignados";
        }
    }

    private void initViews() {
        btnBackGrupoLabs = findViewById(R.id.btnBackGrupoLabs);
        tvNombreUsuarioBarraGrupoLabs = findViewById(R.id.tvNombreUsuarioBarraGrupoLabs);
        tvTituloGrupoLabs = findViewById(R.id.tvTituloGrupoLabs);
        tvSubtituloGrupoLabs = findViewById(R.id.tvSubtituloGrupoLabs);
        tvResumenGrupoLabs = findViewById(R.id.tvResumenGrupoLabs);
        tvEstadoVacioGrupoLabs = findViewById(R.id.tvEstadoVacioGrupoLabs);
        rvLaboratoriosAsignados = findViewById(R.id.rvLaboratoriosAsignados);

        rvLaboratoriosAsignados.setLayoutManager(new LinearLayoutManager(this));
        rvLaboratoriosAsignados.setHasFixedSize(false);
        rvLaboratoriosAsignados.setNestedScrollingEnabled(false);
    }

    private void initListeners() {
        btnBackGrupoLabs.setOnClickListener(v -> finish());
    }

    private void setupHeader() {
        String nombreUsuario = tokenManager.getUserName();

        if (nombreUsuario != null && !nombreUsuario.trim().isEmpty()) {
            tvNombreUsuarioBarraGrupoLabs.setText(nombreUsuario.toUpperCase());
        } else {
            tvNombreUsuarioBarraGrupoLabs.setText("ESTUDIANTE");
        }

        tvTituloGrupoLabs.setText(grupoNombre);
        tvSubtituloGrupoLabs.setText(grupoCurso);
        tvResumenGrupoLabs.setText("Laboratorios asignados a " + grupoNombre + ".");
    }

    private void cargarLaboratorios() {
        mostrarCargando();

        laboratorioRepository.getLaboratoriosAsignadosPorGrupo(grupoId, result -> {
            if (result.isSuccess()) {
                mostrarLaboratorios(result.getData());
            } else {
                mostrarEstadoVacio(result.getErrorMessage());
            }
        });
    }

    private void mostrarLaboratorios(List<LaboratorioAsignadoItem> laboratorios) {
        if (laboratorios == null || laboratorios.isEmpty()) {
            mostrarEstadoVacio("No hay laboratorios asignados para este grupo.");
            return;
        }

        tvEstadoVacioGrupoLabs.setVisibility(View.GONE);
        rvLaboratoriosAsignados.setVisibility(View.VISIBLE);

        LaboratorioAsignadoAdapter adapter = new LaboratorioAsignadoAdapter(
                laboratorios,
                laboratorio -> abrirLaboratorio(laboratorio)
        );

        rvLaboratoriosAsignados.setAdapter(adapter);
    }

    private void abrirLaboratorio(LaboratorioAsignadoItem laboratorio) {
        Intent intent = new Intent(this, DetalleLaboratorioActivity.class);

        intent.putExtra(DetalleLaboratorioActivity.EXTRA_ASIGNACION_ID, laboratorio.getAsignacionId());
        intent.putExtra(DetalleLaboratorioActivity.EXTRA_LABORATORIO_ID, laboratorio.getLaboratorioId());
        intent.putExtra(DetalleLaboratorioActivity.EXTRA_GRUPO_ID, laboratorio.getGrupoId());

        intent.putExtra(DetalleLaboratorioActivity.EXTRA_TITULO, laboratorio.getTitulo());
        intent.putExtra(
                DetalleLaboratorioActivity.EXTRA_RESUMEN,
                "Comprender el comportamiento del tiro parabólico mediante lectura, preguntas, práctica experimental, práctica simulada AR y análisis de resultados."
        );

        intent.putExtra(DetalleLaboratorioActivity.EXTRA_GRUPO_NOMBRE, grupoNombre);
        intent.putExtra(DetalleLaboratorioActivity.EXTRA_ESTADO_ASIGNACION, laboratorio.getEstadoAsignacion());
        intent.putExtra(DetalleLaboratorioActivity.EXTRA_ESTADO_ENTREGA, laboratorio.getEstadoEntrega());
        intent.putExtra(DetalleLaboratorioActivity.EXTRA_FECHA_FIN, laboratorio.getFechaFin());

        intent.putExtra(DetalleLaboratorioActivity.EXTRA_INTENTOS_USADOS, laboratorio.getIntentosUsados());
        intent.putExtra(DetalleLaboratorioActivity.EXTRA_INTENTOS_MAXIMOS, laboratorio.getIntentosMaximos());

        intent.putExtra(DetalleLaboratorioActivity.EXTRA_LAB_KEY, laboratorio.getLabKey());
        intent.putExtra(DetalleLaboratorioActivity.EXTRA_UNITY_SCENE, laboratorio.getUnitySceneName());

        startActivity(intent);
    }

    private void mostrarCargando() {
        rvLaboratoriosAsignados.setVisibility(View.GONE);
        tvEstadoVacioGrupoLabs.setVisibility(View.VISIBLE);
        tvEstadoVacioGrupoLabs.setText("Cargando laboratorios...");
    }

    private void mostrarEstadoVacio(String mensaje) {
        rvLaboratoriosAsignados.setVisibility(View.GONE);
        tvEstadoVacioGrupoLabs.setVisibility(View.VISIBLE);
        tvEstadoVacioGrupoLabs.setText(mensaje);
    }
}