package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;

import org.json.JSONArray;
import org.json.JSONObject;

public class ComparacionResultadosActivity extends AppCompatActivity {

    private ImageView btnBack;

    private TextView tvDistanciaSimulada;
    private TextView tvDistanciaExperimental;
    private TextView tvDiferenciaDistancia;
    private TextView tvResultadoAr;
    private TextView tvResumenDatosExperimentales;

    private EditText etAnalisisComparacion;
    private Button btnGuardarComparacion;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    private double distanciaSimulada = 0.0;
    private double distanciaExperimental = 0.0;
    private boolean tieneDistanciaSimulada = false;
    private boolean tieneDistanciaExperimental = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparacion_resultados);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        initListeners();

        cargarResumenUnity();
        cargarDatosExperimentales();
        cargarAnalisisGuardado();
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackComparacion);

        tvDistanciaSimulada = findViewById(R.id.tvDistanciaSimulada);
        tvDistanciaExperimental = findViewById(R.id.tvDistanciaExperimental);
        tvDiferenciaDistancia = findViewById(R.id.tvDiferenciaDistancia);
        tvResultadoAr = findViewById(R.id.tvResultadoAr);

        tvResumenDatosExperimentales = findViewById(R.id.tvResumenDatosExperimentales);

        etAnalisisComparacion = findViewById(R.id.etAnalisisComparacion);
        btnGuardarComparacion = findViewById(R.id.btnGuardarComparacion);
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnGuardarComparacion.setOnClickListener(v -> guardarAnalisisYCompletar());
    }

    private void cargarResumenUnity() {
        String unityJson = sessionStore.getUnityResultJson(asignacionId);

        if (unityJson == null || unityJson.trim().isEmpty()) {
            tvResultadoAr.setText("No hay resultado AR guardado.");
            tvDistanciaSimulada.setText("-");
            tvDiferenciaDistancia.setText("-");
            return;
        }

        try {
            JSONObject json = new JSONObject(unityJson);

            boolean hitTarget = json.optBoolean("hitTarget", false);
            boolean completed = json.optBoolean("completed", false);

            int usedAttempts = json.optInt("usedAttempts", 0);
            int maxAttempts = json.optInt("maxAttempts", 0);
            int remainingAttempts = json.optInt("remainingAttempts", 0);

            String resultStatus = json.optString("resultStatus", "Sin estado");
            String exitReason = json.optString("exitReason", "");

            distanciaSimulada = json.optDouble("straightDistance", 0.0);
            tieneDistanciaSimulada = true;

            double horizontalDistance = json.optDouble("horizontalDistance", 0.0);
            double verticalDistance = json.optDouble("verticalDistance", 0.0);

            tvDistanciaSimulada.setText(formatMetro(distanciaSimulada));

            tvResultadoAr.setText(
                    "Estado AR: " + resultStatus + "\n" +
                            "Práctica finalizada: " + (completed ? "Sí" : "No") + "\n" +
                            "Impactó objetivo: " + (hitTarget ? "Sí" : "No") + "\n" +
                            "Intentos usados: " + usedAttempts + "/" + maxAttempts + "\n" +
                            "Intentos restantes: " + remainingAttempts + "\n" +
                            "Distancia horizontal: " + formatMetro(horizontalDistance) + "\n" +
                            "Distancia vertical: " + formatMetro(verticalDistance) +
                            (exitReason == null || exitReason.trim().isEmpty()
                                    ? ""
                                    : "\nSalida: " + exitReason)
            );

        } catch (Exception e) {
            e.printStackTrace();

            tvResultadoAr.setText("No se pudo leer el resultado AR guardado.");
            tvDistanciaSimulada.setText("-");
            tvDiferenciaDistancia.setText("-");
        }
    }

    private void cargarDatosExperimentales() {
        String datosJson = sessionStore.getDatosExperimentalesJson(asignacionId);

        if (datosJson == null || datosJson.trim().isEmpty()) {
            tvResumenDatosExperimentales.setText("No hay datos experimentales registrados.");
            tvDistanciaExperimental.setText("-");
            calcularDiferencia();
            return;
        }

        try {
            JSONArray array = new JSONArray(datosJson);

            if (array.length() == 0) {
                tvResumenDatosExperimentales.setText("No hay datos experimentales registrados.");
                tvDistanciaExperimental.setText("-");
                calcularDiferencia();
                return;
            }

            StringBuilder resumen = new StringBuilder();
            resumen.append("Datos registrados: ").append(array.length()).append("\n\n");

            /*
             * Intentamos encontrar una distancia experimental dentro de los datos.
             * Por ahora usamos una extracción flexible buscando números en textos que contengan:
             * distancia, alcance o recorrido.
             */
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;

                String titulo = item.optString("titulo", "Dato experimental");
                String descripcion = item.optString("descripcion", "");

                resumen.append("• ")
                        .append(titulo)
                        .append(": ")
                        .append(descripcion)
                        .append("\n\n");

                intentarExtraerDistanciaExperimental(titulo, descripcion);
            }

            tvResumenDatosExperimentales.setText(resumen.toString().trim());

            if (tieneDistanciaExperimental) {
                tvDistanciaExperimental.setText(formatMetro(distanciaExperimental));
            } else {
                tvDistanciaExperimental.setText("No detectada");
            }

            calcularDiferencia();

        } catch (Exception e) {
            e.printStackTrace();

            tvResumenDatosExperimentales.setText("No se pudieron leer los datos experimentales.");
            tvDistanciaExperimental.setText("-");
            calcularDiferencia();
        }
    }

    private void intentarExtraerDistanciaExperimental(String titulo, String descripcion) {
        if (tieneDistanciaExperimental) {
            return;
        }

        String texto = ((titulo == null ? "" : titulo) + " " + (descripcion == null ? "" : descripcion)).toLowerCase();

        boolean pareceDistancia = texto.contains("distancia")
                || texto.contains("alcance")
                || texto.contains("recorrido")
                || texto.contains("horizontal");

        if (!pareceDistancia) {
            return;
        }

        Double valor = extraerPrimerNumero(texto);

        if (valor != null) {
            distanciaExperimental = valor;
            tieneDistanciaExperimental = true;
        }
    }

    private Double extraerPrimerNumero(String texto) {
        if (texto == null) return null;

        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(?:[\\.,]\\d+)?)");
            java.util.regex.Matcher matcher = pattern.matcher(texto);

            if (matcher.find()) {
                String numero = matcher.group(1).replace(",", ".");
                return Double.parseDouble(numero);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void calcularDiferencia() {
        if (!tieneDistanciaSimulada || !tieneDistanciaExperimental) {
            tvDiferenciaDistancia.setText("-");
            return;
        }

        double diferencia = Math.abs(distanciaSimulada - distanciaExperimental);
        tvDiferenciaDistancia.setText(formatMetro(diferencia));
    }

    private void cargarAnalisisGuardado() {
        String analisis = sessionStore.getComparacionTexto(asignacionId);

        if (analisis != null && !analisis.trim().isEmpty()) {
            etAnalisisComparacion.setText(analisis);
        }
    }

    private void guardarAnalisisYCompletar() {
        String analisis = etAnalisisComparacion.getText().toString().trim();

        if (analisis.isEmpty()) {
            etAnalisisComparacion.setError("Escribe tu análisis");
            return;
        }

        if (analisis.length() < 20) {
            etAnalisisComparacion.setError("Escribe un análisis un poco más completo");
            return;
        }

        sessionStore.saveComparacionTexto(asignacionId, analisis);

        Toast.makeText(this, "Análisis guardado", Toast.LENGTH_SHORT).show();

        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }

    private String formatMetro(double value) {
        return String.format(java.util.Locale.US, "%.2f m", value);
    }
}