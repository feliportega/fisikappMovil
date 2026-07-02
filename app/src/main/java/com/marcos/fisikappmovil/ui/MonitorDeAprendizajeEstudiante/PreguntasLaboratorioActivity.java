package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;

import org.json.JSONArray;
import org.json.JSONObject;

public class PreguntasLaboratorioActivity extends AppCompatActivity {

    private EditText etRespuesta1;
    private EditText etRespuesta2;
    private Button btnGuardar;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    private final String pregunta1 = "¿Cómo afecta el ángulo de lanzamiento al alcance horizontal?";
    private final String pregunta2 = "¿Qué relación existe entre velocidad inicial y altura máxima?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preguntas_laboratorio);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        cargarRespuestasGuardadas();
        initListeners();
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);
    }

    private void initViews() {
        etRespuesta1 = findViewById(R.id.etRespuesta1);
        etRespuesta2 = findViewById(R.id.etRespuesta2);
        btnGuardar = findViewById(R.id.btnGuardarPreguntas);
    }

    private void initListeners() {
        btnGuardar.setOnClickListener(v -> guardarRespuestas());
    }

    private void cargarRespuestasGuardadas() {
        String jsonGuardado = sessionStore.getPreguntasJson(asignacionId);

        if (jsonGuardado == null || jsonGuardado.trim().isEmpty()) {
            return;
        }

        try {
            JSONArray array = new JSONArray(jsonGuardado);

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;

                int id = item.optInt("id", -1);
                String respuesta = item.optString("respuesta", "");

                if (id == 1) {
                    etRespuesta1.setText(respuesta);
                } else if (id == 2) {
                    etRespuesta2.setText(respuesta);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarRespuestas() {
        String respuesta1 = etRespuesta1.getText().toString().trim();
        String respuesta2 = etRespuesta2.getText().toString().trim();

        if (respuesta1.isEmpty()) {
            etRespuesta1.setError("Responde esta pregunta");
            return;
        }

        if (respuesta2.isEmpty()) {
            etRespuesta2.setError("Responde esta pregunta");
            return;
        }

        try {
            JSONArray array = new JSONArray();

            JSONObject item1 = new JSONObject();
            item1.put("id", 1);
            item1.put("pregunta", pregunta1);
            item1.put("respuesta", respuesta1);
            array.put(item1);

            JSONObject item2 = new JSONObject();
            item2.put("id", 2);
            item2.put("pregunta", pregunta2);
            item2.put("respuesta", respuesta2);
            array.put(item2);

            sessionStore.savePreguntasJson(asignacionId, array.toString());

            completarPaso();

        } catch (Exception e) {
            e.printStackTrace();
            etRespuesta1.setError("No se pudieron guardar las respuestas");
        }
    }

    private void completarPaso() {
        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }
}