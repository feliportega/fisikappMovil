package com.marcos.fisikappmovil.ui.Laboratorio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.PasosLaboratorio;

public class DetalleLaboratorioActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_laboratorio);

        readExtras();
        initViews();
        initListeners();
        pintarDatos();
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
            titulo = "Tiro parabólico";
        }

        if (resumen == null || resumen.trim().isEmpty()) {
            resumen = "Comprender el comportamiento del tiro parabólico observando la trayectoria de un objeto lanzado con un ángulo inicial determinado.";
        }

        if (grupoNombre == null || grupoNombre.trim().isEmpty()) {
            grupoNombre = "Grupo académico";
        }

        if (estadoAsignacion == null || estadoAsignacion.trim().isEmpty()) {
            estadoAsignacion = "ABIERTO";
        }

        if (estadoEntrega == null || estadoEntrega.trim().isEmpty()) {
            estadoEntrega = "PENDIENTE";
        }

        if (fechaFin == null || fechaFin.trim().isEmpty()) {
            fechaFin = "Sin fecha límite";
        }

        if (labKey == null || labKey.trim().isEmpty()) {
            labKey = "PARABOLIC-001";
        }

        if (unitySceneName == null || unitySceneName.trim().isEmpty()) {
            unitySceneName = "ParabolicMotionLab";
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
    }

    private void initListeners() {
        btnBackDetalleLab.setOnClickListener(v -> finish());

        btnComenzarPasosLab.setOnClickListener(v -> abrirPasosLaboratorio());
    }

    private void pintarDatos() {
        tvDetalleTituloLab.setText(titulo);
        tvDetalleSubtituloLab.setText("Laboratorio asignado");
        tvDetalleObjetivoLab.setText(resumen);

        tvDetalleGrupoLab.setText("Grupo: " + grupoNombre);
        tvDetalleEstadoLab.setText("Estado: " + estadoAsignacion);
        tvDetalleEntregaLab.setText("Entrega: " + estadoEntrega);
        tvDetalleIntentosLab.setText("Intentos: " + intentosUsados + "/" + intentosMaximos);
        tvDetalleFechaLab.setText("Fecha límite: " + fechaFin);

        tvDetalleUnityLab.setText(labKey + " · " + unitySceneName);
    }

    private void abrirPasosLaboratorio() {
        if (laboratorioId == -1) {
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
}