package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.repository.LaboratorioRepository;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.SimulacionARActivity;
import com.marcos.fisikappmovil.model.LaboratorioPasoItem;
import com.marcos.fisikappmovil.ui.Laboratorio.PasoLaboratorioAdapter;

import java.util.List;

public class PasosLaboratorio extends AppCompatActivity {

    public static final String EXTRA_ASIGNACION_ID = "ASIGNACION_ID";
    public static final String EXTRA_LABORATORIO_ID = "LABORATORIO_ID";
    public static final String EXTRA_GRUPO_ID = "GRUPO_ID";

    public static final String EXTRA_ORDEN_PASO = "ORDEN_PASO";
    public static final String EXTRA_TIPO_PASO = "TIPO_PASO";

    private ImageView btnBackPasosLab;
    private TextView tvEstadoPasosLab;
    private RecyclerView rvPasosLaboratorio;

    private LaboratorioRepository laboratorioRepository;
    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;

    private PasoLaboratorioAdapter adapter;
    private List<LaboratorioPasoItem> pasosActuales;

    private final ActivityResultLauncher<Intent> pasoLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            int ordenCompletado = result.getData().getIntExtra(EXTRA_ORDEN_PASO, -1);

                            if (ordenCompletado != -1) {
                                sessionStore.completarPasoYDesbloquearSiguiente(
                                        asignacionId,
                                        ordenCompletado
                                );

                                cargarPasos();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasos_laboratorio);

        initDependencies();
        readExtras();
        initViews();
        initListeners();
        cargarPasos();
    }

    private void initDependencies() {
        laboratorioRepository = new LaboratorioRepository();
        sessionStore = new LaboratorioSessionStore(this);
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(EXTRA_GRUPO_ID, -1);

        if (laboratorioId == -1) {
            laboratorioId = intent.getIntExtra("LABORATORIO_ID", -1);
        }
    }

    private void initViews() {
        btnBackPasosLab = findViewById(R.id.btnBackPasosLab);
        tvEstadoPasosLab = findViewById(R.id.tvEstadoPasosLab);
        rvPasosLaboratorio = findViewById(R.id.rvPasosLaboratorio);

        rvPasosLaboratorio.setLayoutManager(new LinearLayoutManager(this));
        rvPasosLaboratorio.setNestedScrollingEnabled(false);
    }

    private void initListeners() {
        btnBackPasosLab.setOnClickListener(v -> finish());
    }

    private void cargarPasos() {
        tvEstadoPasosLab.setText("Cargando pasos...");

        laboratorioRepository.getPasosLaboratorioMock(asignacionId, laboratorioId, result -> {
            if (!result.isSuccess()) {
                tvEstadoPasosLab.setText(result.getErrorMessage());
                return;
            }

            pasosActuales = result.getData();
            aplicarEstadosGuardados(pasosActuales);
            mostrarPasos(pasosActuales);
        });
    }

    private void aplicarEstadosGuardados(List<LaboratorioPasoItem> pasos) {
        if (pasos == null) return;

        for (LaboratorioPasoItem paso : pasos) {
            String estadoGuardado = sessionStore.getEstadoPaso(asignacionId, paso.getOrden());
            paso.setEstado(estadoGuardado);
        }
    }

    private void mostrarPasos(List<LaboratorioPasoItem> pasos) {
        if (pasos == null || pasos.isEmpty()) {
            tvEstadoPasosLab.setText("Este laboratorio no tiene pasos configurados.");
            return;
        }

        tvEstadoPasosLab.setText("Completa cada paso en orden para enviar tu laboratorio.");

        adapter = new PasoLaboratorioAdapter(
                pasos,
                this::abrirPaso
        );

        rvPasosLaboratorio.setAdapter(adapter);
    }

    private void abrirPaso(LaboratorioPasoItem paso) {
        if (paso.estaBloqueado()) {
            Toast.makeText(this, "Completa el paso anterior primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent;

        switch (paso.getTipo()) {
            case LaboratorioPasoItem.TIPO_LECTURA:
                intent = new Intent(this, LecturaLaboratorioActivity.class);
                break;

            case LaboratorioPasoItem.TIPO_PREGUNTAS:
                intent = new Intent(this, PreguntasLaboratorioActivity.class);
                break;

            case LaboratorioPasoItem.TIPO_PRACTICA_EXPERIMENTAL:
                intent = new Intent(this, PracticaExperimental.class);
                break;

            case LaboratorioPasoItem.TIPO_DATOS_EXPERIMENTALES:
                intent = new Intent(this, DatosExperimentalesActivity.class);
                break;

            case LaboratorioPasoItem.TIPO_SIMULACION_AR:
                intent = new Intent(this, SimulacionARActivity.class);
                intent.putExtra("LAB_KEY", "PARABOLIC-001");
                intent.putExtra("UNITY_SCENE", "ParabolicMotionLab");
                break;

            case LaboratorioPasoItem.TIPO_COMPARACION:
                intent = new Intent(this, ComparacionResultadosActivity.class);
                break;

            case LaboratorioPasoItem.TIPO_INFORME:
                intent = new Intent(this, InformeLaboratorio.class);
                break;

            case LaboratorioPasoItem.TIPO_ENVIO:
                intent = new Intent(this, EnvioEntregaLaboratorioActivity.class);
                break;

            default:
                Toast.makeText(this, "Tipo de paso no soportado.", Toast.LENGTH_SHORT).show();
                return;
        }

        intent.putExtra(EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(EXTRA_ORDEN_PASO, paso.getOrden());
        intent.putExtra(EXTRA_TIPO_PASO, paso.getTipo());

        pasoLauncher.launch(intent);
    }
}