package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.repository.LaboratorioRepository;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
import com.marcos.fisikappmovil.remote.response.MobileResourceResponse;
import com.marcos.fisikappmovil.remote.response.MobileStepResponse;
import com.marcos.fisikappmovil.ui.Laboratorio.RenderContentActivity;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.SimulacionARActivity;
import com.marcos.fisikappmovil.model.LaboratorioPasoItem;
import com.marcos.fisikappmovil.ui.Laboratorio.PasoLaboratorioAdapter;

import java.util.ArrayList;
import java.util.Collections;
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

    private Button btnEnviarLaboratorio;
    private TextView tvEntregaEnviada;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;

    private PasoLaboratorioAdapter adapter;
    private List<LaboratorioPasoItem> pasosActuales;

    private Gson gson;

    private String mapMobileTypeToPasoTipo(String type) {
        if (type == null) return LaboratorioPasoItem.TIPO_LECTURA;

        switch (type) {
            case "INTRODUCTION":
            case "THEORY":
            case "OBJECTIVES":
            case "CONCEPTS":
            case "FORMULAS":
            case "PROCEDURES":
                return LaboratorioPasoItem.TIPO_LECTURA;

            case "QUESTIONS":
                return LaboratorioPasoItem.TIPO_PREGUNTAS;

            case "EXPERIMENTAL_PRACTICE":
            case "PRACTICE":
            case "GUIDED_PRACTICE":
                return LaboratorioPasoItem.TIPO_PRACTICA_EXPERIMENTAL;

            case "SIMULATION_AR":
                return LaboratorioPasoItem.TIPO_SIMULACION_AR;

            case "COMPARISON":
                return LaboratorioPasoItem.TIPO_COMPARACION;

            case "REPORT":
                return LaboratorioPasoItem.TIPO_INFORME;

            case "SUBMISSION":
                return LaboratorioPasoItem.TIPO_ENVIO;

            default:
                return LaboratorioPasoItem.TIPO_LECTURA;
        }
    }

    private List<MobileStepResponse> mobileStepsActuales = new ArrayList<>();

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

                                cargarPasosDesdeMobileResource();
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
        cargarPasosDesdeMobileResource();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sessionStore != null && laboratorioRepository != null) {
            cargarPasosDesdeMobileResource();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        readExtras();

        if (sessionStore != null && laboratorioRepository != null) {
            cargarPasosDesdeMobileResource();
        }
    }

    private void initDependencies() {
        laboratorioRepository = new LaboratorioRepository();
        sessionStore = new LaboratorioSessionStore(this);
        gson = new Gson();
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
        //rvPasosLaboratorio.setNestedScrollingEnabled(false);

        btnEnviarLaboratorio = findViewById(R.id.btnEnviarLaboratorio);
        tvEntregaEnviada = findViewById(R.id.tvEntregaEnviada);
    }

    private void initListeners() {
        btnBackPasosLab.setOnClickListener(v -> finish());
        btnEnviarLaboratorio.setOnClickListener(v -> abrirEnvioLaboratorio());
    }

    private void cargarPasosDesdeMobileResource() {
        String json = sessionStore.getMobileResourceJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            tvEstadoPasosLab.setText("No se encontró información del laboratorio. Vuelve al detalle e intenta cargar nuevamente.");
            btnEnviarLaboratorio.setVisibility(View.GONE);
            tvEntregaEnviada.setVisibility(View.GONE);
            rvPasosLaboratorio.setAdapter(null);
            return;
        }

        try {
            MobileResourceResponse response = gson.fromJson(json, MobileResourceResponse.class);

            if (response == null || response.getSteps() == null || response.getSteps().isEmpty()) {
                tvEstadoPasosLab.setText("Este laboratorio no tiene pasos configurados.");
                btnEnviarLaboratorio.setVisibility(View.GONE);
                tvEntregaEnviada.setVisibility(View.GONE);
                rvPasosLaboratorio.setAdapter(null);
                return;
            }

            List<MobileStepResponse> steps = new ArrayList<>(response.getSteps());

            Collections.sort(steps, (a, b) -> Integer.compare(a.getOrder(), b.getOrder()));

            mostrarPasosMobile(steps);

        } catch (Exception e) {
            tvEstadoPasosLab.setText("No se pudo leer la información guardada del laboratorio.");
            btnEnviarLaboratorio.setVisibility(View.GONE);
            tvEntregaEnviada.setVisibility(View.GONE);
            rvPasosLaboratorio.setAdapter(null);
        }
    }

    private void mostrarPasosMobile(List<MobileStepResponse> steps) {
        mobileStepsActuales = steps;

        if (steps == null || steps.isEmpty()) {
            tvEstadoPasosLab.setText("Este laboratorio no tiene pasos configurados.");
            btnEnviarLaboratorio.setVisibility(View.GONE);
            tvEntregaEnviada.setVisibility(View.GONE);
            return;
        }

        List<LaboratorioPasoItem> pasos = new ArrayList<>();

        for (MobileStepResponse step : steps) {
            if (step == null) continue;

            LaboratorioPasoItem item = new LaboratorioPasoItem(
                    step.getOrder(),
                    safe(step.getTitle()),
                    buildDescripcionMobileStep(step),
                    mapMobileTypeToPasoTipo(step.getType()),
                    step.isRequired(),
                    resolverEstadoInicial(step.getOrder())
            );

            pasos.add(item);
        }

        pasosActuales = pasos;
        mostrarPasos(pasosActuales);
    }
    private void mostrarPasos(List<LaboratorioPasoItem> pasos) {

        if (pasos != null) {
            for (LaboratorioPasoItem paso : pasos) {
                android.util.Log.d(
                        "PASOS_DEBUG",
                        "Paso " + paso.getOrden()
                                + " | " + paso.getTitulo()
                                + " | tipo=" + paso.getTipo()
                                + " | estado=" + paso.getEstado()
                );
            }
        }

        if (pasos == null || pasos.isEmpty()) {
            tvEstadoPasosLab.setText("Este laboratorio no tiene pasos configurados.");
            btnEnviarLaboratorio.setVisibility(View.GONE);
            tvEntregaEnviada.setVisibility(View.GONE);
            return;
        }

        tvEstadoPasosLab.setText("Completa cada paso en orden para enviar tu laboratorio.");

        adapter = new PasoLaboratorioAdapter(
                pasos,
                this::abrirPaso
        );

        rvPasosLaboratorio.setAdapter(adapter);
        actualizarBotonEnvio(pasos);
    }

    private void abrirPasoMobile(MobileStepResponse step) {
        if (step == null || step.getType() == null) {
            Toast.makeText(this, "Paso no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (step.getType()) {
            case "INTRODUCTION":
            case "THEORY":
            case "OBJECTIVES":
            case "CONCEPTS":
            case "PROCEDURES":
                abrirRenderContent(step);
                break;
            case "FORMULAS":
                abrirRenderContent(step);
                break;

            case "SIMULATION_AR":
                abrirSimulacionAr(step);
                break;

            case "COMPARISON":
                abrirComparacion(step);
                break;

            case "REPORT":
                abrirInforme(step);
                break;

            case "SUBMISSION":
                abrirEnvioEntrega(step);
                break;

            case "EXPERIMENTAL_PRACTICE":
                abrirPracticaExperimental(step);
                break;

            default:
                Toast.makeText(this, "Este paso todavía no está disponible.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void abrirPaso(LaboratorioPasoItem paso) {
        if (paso.estaBloqueado()) {
            Toast.makeText(this, "Completa el paso anterior primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        MobileStepResponse mobileStep = findMobileStepByOrder(paso.getOrden());

        if (mobileStep != null) {
            abrirPasoMobile(mobileStep);
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

    private void abrirRenderContent(MobileStepResponse step) {
        Intent intent = new Intent(this, RenderContentActivity.class);

        intent.putExtra(RenderContentActivity.EXTRA_ASSIGNMENT_ID, asignacionId);
        intent.putExtra(RenderContentActivity.EXTRA_STEP_ID, step.getId());
        intent.putExtra(RenderContentActivity.EXTRA_STEP_TITLE, step.getTitle());

        intent.putExtra(EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(EXTRA_ORDEN_PASO, step.getOrder());
        intent.putExtra(EXTRA_TIPO_PASO, step.getType());

        pasoLauncher.launch(intent);
    }
    private void abrirEnvioLaboratorio() {
        Intent intent = new Intent(this, EnvioEntregaLaboratorioActivity.class);

        intent.putExtra(EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(EXTRA_GRUPO_ID, grupoId);

        intent.putExtra("GRUPO_NOMBRE", getIntent().getStringExtra("GRUPO_NOMBRE"));
        intent.putExtra("GRUPO_CURSO", getIntent().getStringExtra("GRUPO_CURSO"));

        intent.putExtra(EXTRA_ORDEN_PASO, -1);

        pasoLauncher.launch(intent);
    }

    private void actualizarBotonEnvio(List<LaboratorioPasoItem> pasos) {
        boolean entregaEnviada = sessionStore.isEntregaEnviada(asignacionId);

        if (entregaEnviada) {
            btnEnviarLaboratorio.setVisibility(View.GONE);
            tvEntregaEnviada.setVisibility(View.VISIBLE);
            tvEstadoPasosLab.setText("Laboratorio enviado. La calificación está pendiente.");
            return;
        }

        tvEntregaEnviada.setVisibility(View.GONE);

        boolean todosCompletados = true;

        for (LaboratorioPasoItem paso : pasos) {
            if (paso.isObligatorio() && !paso.estaCompletado()) {
                todosCompletados = false;
                android.util.Log.d(
                        "PASOS_DEBUG",
                        "Falta completar paso: "
                                + paso.getOrden()
                                + " - "
                                + paso.getTitulo()
                                + " estado="
                                + paso.getEstado()
                );
                break;
            }
        }

        if (todosCompletados) {
            btnEnviarLaboratorio.setVisibility(View.VISIBLE);
            tvEstadoPasosLab.setText("Todos los pasos están completos. Ya puedes enviar el laboratorio.");
        } else {
            btnEnviarLaboratorio.setVisibility(View.GONE);
            tvEstadoPasosLab.setText("Completa cada paso en orden para enviar tu laboratorio.");
        }
    }

    private MobileStepResponse findMobileStepByOrder(int order) {
        if (mobileStepsActuales == null || mobileStepsActuales.isEmpty()) {
            return null;
        }

        for (MobileStepResponse step : mobileStepsActuales) {
            if (step != null && step.getOrder() == order) {
                return step;
            }
        }

        return null;
    }

    private void abrirSimulacionAr(MobileStepResponse step) {
        Intent intent = new Intent(this, SimulacionARActivity.class);

        intent.putExtra(EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(EXTRA_ORDEN_PASO, step.getOrder());
        intent.putExtra(EXTRA_TIPO_PASO, step.getType());

        intent.putExtra("LAB_KEY", "PARABOLIC-001");
        intent.putExtra("UNITY_SCENE", "ParabolicMotionLab");

        pasoLauncher.launch(intent);
    }

    private void abrirComparacion(MobileStepResponse step) {
        Intent intent = new Intent(this, ComparacionResultadosActivity.class);

        intent.putExtra(EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(EXTRA_ORDEN_PASO, step.getOrder());
        intent.putExtra(EXTRA_TIPO_PASO, step.getType());

        pasoLauncher.launch(intent);
    }

    private void abrirInforme(MobileStepResponse step) {
        Intent intent = new Intent(this, InformeLaboratorio.class);

        intent.putExtra(EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(EXTRA_ORDEN_PASO, step.getOrder());
        intent.putExtra(EXTRA_TIPO_PASO, step.getType());

        pasoLauncher.launch(intent);
    }

    private void abrirEnvioEntrega(MobileStepResponse step) {
        Intent intent = new Intent(this, EnvioEntregaLaboratorioActivity.class);

        intent.putExtra(EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(EXTRA_ORDEN_PASO, step.getOrder());
        intent.putExtra(EXTRA_TIPO_PASO, step.getType());

        intent.putExtra("GRUPO_NOMBRE", getIntent().getStringExtra("GRUPO_NOMBRE"));
        intent.putExtra("GRUPO_CURSO", getIntent().getStringExtra("GRUPO_CURSO"));

        pasoLauncher.launch(intent);
    }

    private void abrirPracticaExperimental(MobileStepResponse step) {
        Intent intent = new Intent(this, PracticaExperimental.class);

        intent.putExtra(EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(EXTRA_ORDEN_PASO, step.getOrder());
        intent.putExtra(EXTRA_TIPO_PASO, step.getType());

        pasoLauncher.launch(intent);
    }

    private String buildDescripcionMobileStep(MobileStepResponse step) {
        if (step == null) return "";

        String type = step.getType();

        if (type == null) return "";

        switch (type) {
            case "INTRODUCTION":
                return "Lee la introducción del laboratorio.";

            case "THEORY":
                return "Revisa el marco teórico.";

            case "OBJECTIVES":
                return "Consulta el objetivo general y los objetivos específicos.";

            case "CONCEPTS":
                return "Revisa los conceptos básicos del laboratorio.";

            case "PROCEDURES":
                return "Revisa el procedimiento que debes seguir en el laboratorio.";

            case "FORMULAS":
                return "Consulta las fórmulas necesarias para la práctica.";

            case "SIMULATION_AR":
                return "Realiza la simulación en realidad aumentada.";

            case "COMPARISON":
                return "Compara los resultados obtenidos.";

            case "REPORT":
                return "Completa el informe final del laboratorio.";

            case "SUBMISSION":
                return "Envía la entrega del laboratorio.";

            case "EXPERIMENTAL_PRACTICE":
                return "Realiza y registra la práctica experimental.";

            default:
                return "Completa este paso del laboratorio.";
        }
    }

    private String resolverEstadoInicial(int order) {
        String estadoGuardado = sessionStore.getEstadoPaso(asignacionId, order);

        if (estadoGuardado != null && !estadoGuardado.trim().isEmpty()) {
            return estadoGuardado;
        }

        if (order == 1) {
            return LaboratorioPasoItem.ESTADO_DISPONIBLE;
        }

        return LaboratorioPasoItem.ESTADO_BLOQUEADO;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

}