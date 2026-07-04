package com.marcos.fisikappmovil.ui.Laboratorio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.repository.LaboratorioRepository;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.remote.response.MobileResourceResponse;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.PasosLaboratorio;
import com.marcos.fisikappmovil.ui.common.ContentStateView;

import org.json.JSONObject;

public class DetalleLaboratorioActivity extends AppCompatActivity {

    private LaboratorioRepository laboratorioRepository;
    private LaboratorioSessionStore sessionStore;
    private TokenManager tokenManager;
    private final Gson gson = new Gson();

    public static final String EXTRA_ASIGNACION_ID = "ASIGNACION_ID";
    public static final String EXTRA_LABORATORIO_ID = "LABORATORIO_ID";
    public static final String EXTRA_GRUPO_ID = "GRUPO_ID";

    public static final String EXTRA_TITULO = "TITULO";
    public static final String EXTRA_RESUMEN = "RESUMEN";
    public static final String EXTRA_GRUPO_NOMBRE = "GRUPO_NOMBRE";

    public static final String EXTRA_ESTADO_ASIGNACION = "ESTADO_ASIGNACION";
    public static final String EXTRA_ESTADO_ENTREGA = "ESTADO_ENTREGA";
    public static final String EXTRA_FECHA_FIN = "FECHA_FIN";

    public static final String EXTRA_INTENTOS_USADOS = "INTENTOS_USADOS";
    public static final String EXTRA_INTENTOS_MAXIMOS = "INTENTOS_MAXIMOS";

    public static final String EXTRA_LAB_KEY = "LAB_KEY";
    public static final String EXTRA_UNITY_SCENE = "UNITY_SCENE";

    private ImageView btnBackDetalleLab;

    private TextView tvDetalleTituloLab;
    private TextView tvDetalleSubtituloLab;
    private TextView tvDetalleObjetivoLab;
    private TextView tvDetalleGrupoLab;
    private TextView tvDetalleEstadoLab;
    private TextView tvDetalleEntregaLab;
    private TextView tvDetalleIntentosLab;
    private TextView tvDetalleFechaLab;
    private TextView tvDetalleUnityLab;

    private Button btnComenzarPasosLab;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;

    private int intentosUsados = 0;
    private int intentosMaximos = 4;

    private String titulo;
    private String resumen;
    private String grupoNombre;
    private String estadoAsignacion;
    private String estadoEntrega;
    private String fechaFin;
    private String labKey;
    private String unitySceneName;

    private boolean detalleCargado = false;
    private View contentDetalleLaboratorio;
    private ContentStateView stateDetalleLaboratorio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_laboratorio);

        laboratorioRepository = new LaboratorioRepository();
        sessionStore = new LaboratorioSessionStore(this);
        tokenManager = new TokenManager(this);

        readExtras();
        initViews();
        initListeners();

        cargarDetalleMobileResource();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (detalleCargado) {
            actualizarEstadoLocal();
        }
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(EXTRA_GRUPO_ID, -1);

        intentosUsados = intent.getIntExtra(EXTRA_INTENTOS_USADOS, 0);
        intentosMaximos = intent.getIntExtra(EXTRA_INTENTOS_MAXIMOS, 4);

        titulo = intent.getStringExtra(EXTRA_TITULO);
        resumen = intent.getStringExtra(EXTRA_RESUMEN);
        grupoNombre = intent.getStringExtra(EXTRA_GRUPO_NOMBRE);
        estadoAsignacion = intent.getStringExtra(EXTRA_ESTADO_ASIGNACION);
        estadoEntrega = intent.getStringExtra(EXTRA_ESTADO_ENTREGA);
        fechaFin = intent.getStringExtra(EXTRA_FECHA_FIN);
        labKey = intent.getStringExtra(EXTRA_LAB_KEY);
        unitySceneName = intent.getStringExtra(EXTRA_UNITY_SCENE);

        if (titulo == null || titulo.trim().isEmpty()) {
            titulo = "No encontrado";
        }

        if (resumen == null || resumen.trim().isEmpty()) {
            resumen = "No encontrado";
        }

        if (grupoNombre == null || grupoNombre.trim().isEmpty()) {
            grupoNombre = "No encontrado";
        }

        if (estadoAsignacion == null || estadoAsignacion.trim().isEmpty()) {
            estadoAsignacion = "No encontrado";
        }

        if (estadoEntrega == null || estadoEntrega.trim().isEmpty()) {
            estadoEntrega = "No encontrado";
        }

        if (fechaFin == null || fechaFin.trim().isEmpty()) {
            fechaFin = "No encontrado";
        }

        if (labKey == null || labKey.trim().isEmpty()) {
            labKey = "No encontrado";
        }

        if (unitySceneName == null || unitySceneName.trim().isEmpty()) {
            unitySceneName = "No encontrado";
        }
    }

    private void initViews() {
        btnBackDetalleLab = findViewById(R.id.btnBackDetalleLab);

        tvDetalleTituloLab = findViewById(R.id.tvDetalleTituloLab);
        tvDetalleSubtituloLab = findViewById(R.id.tvDetalleSubtituloLab);
        tvDetalleObjetivoLab = findViewById(R.id.tvDetalleObjetivoLab);
        tvDetalleGrupoLab = findViewById(R.id.tvDetalleGrupoLab);
        tvDetalleEstadoLab = findViewById(R.id.tvDetalleEstadoLab);
        tvDetalleEntregaLab = findViewById(R.id.tvDetalleEntregaLab);
        tvDetalleIntentosLab = findViewById(R.id.tvDetalleIntentosLab);
        tvDetalleFechaLab = findViewById(R.id.tvDetalleFechaLab);
        tvDetalleUnityLab = findViewById(R.id.tvDetalleUnityLab);

        btnComenzarPasosLab = findViewById(R.id.btnComenzarPasosLab);

        contentDetalleLaboratorio = findViewById(R.id.contentDetalleLaboratorio);
        stateDetalleLaboratorio = new ContentStateView(findViewById(R.id.contentStateDetalleLaboratorio));
    }

    private void initListeners() {
        btnBackDetalleLab.setOnClickListener(v -> finish());

        btnComenzarPasosLab.setOnClickListener(v -> abrirPasosLaboratorio());
    }

    private void abrirPasosLaboratorio() {
        if (asignacionId <= 0) {
            Toast.makeText(this, "No se recibió la asignación del laboratorio.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (laboratorioId <= 0) {
            Toast.makeText(this, "No se recibió el laboratorio.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PasosLaboratorio.class);

        intent.putExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, asignacionId);
        intent.putExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, laboratorioId);
        intent.putExtra(PasosLaboratorio.EXTRA_GRUPO_ID, grupoId);

        intent.putExtra("TITULO", titulo);
        intent.putExtra("LAB_KEY", labKey);
        intent.putExtra("UNITY_SCENE", unitySceneName);

        startActivity(intent);
    }

    private void actualizarEstadoLocal() {
        LaboratorioSessionStore store = new LaboratorioSessionStore(this);
        String unityJson = store.getUnityResultJson(asignacionId);

        if (unityJson == null || unityJson.trim().isEmpty()) {
            tvDetalleIntentosLab.setText("Intentos: " + intentosUsados + "/" + intentosMaximos);
            return;
        }

        try {
            JSONObject json = new JSONObject(unityJson);

            int used = json.optInt("usedAttempts", intentosUsados);
            int max = json.optInt("maxAttempts", intentosMaximos);
            boolean completed = json.optBoolean("completed", false);
            int remaining = json.optInt("remainingAttempts", max - used);

            tvDetalleIntentosLab.setText("Intentos AR: " + used + "/" + max);

            if (completed || remaining <= 0) {
                tvDetalleEntregaLab.setText("Entrega: EN DESARROLLO - AR FINALIZADO");
            } else {
                tvDetalleEntregaLab.setText("Entrega: EN DESARROLLO - AR INCOMPLETO");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarDetalleMobileResource() {
        String authHeader = tokenManager.getAuthorizationHeader();

        if (authHeader == null || authHeader.trim().isEmpty()) {
            Toast.makeText(this, "Sesión no válida.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (asignacionId <= 0) {
            mostrarErrorDetalle("No se recibió la asignación del laboratorio.");
            return;
        }

        mostrarCargaDetalle();

        laboratorioRepository.getMobileResource(authHeader, asignacionId, result -> {
            runOnUiThread(() -> {
                if (!result.isSuccess()) {
                    cargarDesdeCacheSiExiste(result.getErrorMessage());
                    return;
                }

                MobileResourceResponse response = result.getData();

                if (response == null) {
                    mostrarVacioDetalle("El servidor no devolvió información para este laboratorio.");
                    return;
                }

                String json = gson.toJson(response);
                sessionStore.saveMobileResourceJson(asignacionId, json);

                mostrarDetalle(response);
                mostrarContenidoDetalle();
            });
        });
    }
    private void cargarDesdeCacheSiExiste(String errorMessage) {
        String cachedJson = sessionStore.getMobileResourceJson(asignacionId);

        if (cachedJson == null || cachedJson.trim().isEmpty()) {
            mostrarErrorDetalle(errorMessage);
            return;
        }

        try {
            MobileResourceResponse cachedResponse =
                    gson.fromJson(cachedJson, MobileResourceResponse.class);

            mostrarDetalle(cachedResponse);
            mostrarContenidoDetalle();

            Toast.makeText(
                    this,
                    "Mostrando última información guardada.",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {
            mostrarErrorDetalle("No se pudo cargar la información guardada del laboratorio.");
        }
    }
    private void mostrarDetalle(MobileResourceResponse response) {
        if (response == null || response.getResource() == null) {
            Toast.makeText(this, "Laboratorio sin información disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        titulo = response.getResource().getTitle();
        resumen = response.getResource().getSummary();

        String categoria = response.getResource().getCategory();
        String docente = response.getResource().getTeacher();

        if (response.getAssignment() != null) {
            grupoNombre = response.getAssignment().getGroupName();
            fechaFin = response.getAssignment().getDueDate();
            estadoAsignacion = response.getAssignment().getStatus();
        }

        if (estadoEntrega == null || estadoEntrega.trim().isEmpty()) {
            estadoEntrega = "No encontrado";
        }

        if (labKey == null || labKey.trim().isEmpty()) {
            labKey = "No encontrado";
        }

        if (unitySceneName == null || unitySceneName.trim().isEmpty()) {
            unitySceneName = "No encontrado";
        }

        tvDetalleTituloLab.setText(safe(titulo));
        tvDetalleSubtituloLab.setText("Categoría: " + safe(categoria) + " · Docente: " + safe(docente));
        tvDetalleObjetivoLab.setText(safe(resumen));

        tvDetalleGrupoLab.setText("Grupo: " + safe(grupoNombre));
        tvDetalleEstadoLab.setText("Estado: " + safe(estadoAsignacion));
        tvDetalleEntregaLab.setText("Entrega: " + safe(estadoEntrega));
        tvDetalleIntentosLab.setText("Intentos: " + intentosUsados + "/" + intentosMaximos);
        tvDetalleFechaLab.setText("Fecha límite: " + safe(fechaFin));

        tvDetalleUnityLab.setText(safe(labKey) + " · " + safe(unitySceneName));

        detalleCargado = true;
    }

    // Helpers
    private String safe(String value) {
        return value == null ? "" : value;
    }


    private void mostrarCargaDetalle() {
        contentDetalleLaboratorio.setVisibility(View.GONE);

        stateDetalleLaboratorio.showLoading(
                "Cargando laboratorio",
                "Estamos consultando la información del laboratorio asignado."
        );
    }

    private void mostrarContenidoDetalle() {
        stateDetalleLaboratorio.hide();
        contentDetalleLaboratorio.setVisibility(View.VISIBLE);
    }

    private void mostrarErrorDetalle(String mensaje) {
        contentDetalleLaboratorio.setVisibility(View.GONE);

        stateDetalleLaboratorio.showError(
                "No se pudo cargar el laboratorio",
                mensaje,
                v -> cargarDetalleMobileResource()
        );
    }

    private void mostrarVacioDetalle(String mensaje) {
        contentDetalleLaboratorio.setVisibility(View.GONE);

        stateDetalleLaboratorio.showEmpty(
                "Laboratorio sin información",
                mensaje
        );
    }

}