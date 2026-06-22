package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;

import org.json.JSONArray;
import org.json.JSONObject;

public class DatosExperimentalesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnAgregarDato;
    private Button btnCompletarDatos;
    private LinearLayout layoutDatosContainer;
    private TextView tvEstadoDatos;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    private JSONArray datosArray = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_experimentales);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        initListeners();
        cargarDatosGuardados();
        renderDatos();
    }

    private void readExtras() {
        Intent intent = getIntent();

        asignacionId = intent.getIntExtra(PasosLaboratorio.EXTRA_ASIGNACION_ID, -1);
        laboratorioId = intent.getIntExtra(PasosLaboratorio.EXTRA_LABORATORIO_ID, -1);
        grupoId = intent.getIntExtra(PasosLaboratorio.EXTRA_GRUPO_ID, -1);
        ordenPaso = intent.getIntExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, -1);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackDatosExperimentales);
        btnAgregarDato = findViewById(R.id.btnAgregarDatoExperimental);
        btnCompletarDatos = findViewById(R.id.btnCompletarDatosExperimentales);
        layoutDatosContainer = findViewById(R.id.layoutDatosExperimentalesContainer);
        tvEstadoDatos = findViewById(R.id.tvEstadoDatosExperimentales);
    }

    private void initListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAgregarDato.setOnClickListener(v -> mostrarDialogoDato(-1, null, null));

        btnCompletarDatos.setOnClickListener(v -> {
            if (datosArray.length() == 0) {
                tvEstadoDatos.setText("Agrega al menos un dato experimental.");
                return;
            }

            guardarDatos();
            completarPaso();
        });
    }

    private void cargarDatosGuardados() {
        String json = sessionStore.getDatosExperimentalesJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            datosArray = new JSONArray();
            return;
        }

        try {
            datosArray = new JSONArray(json);
        } catch (Exception e) {
            e.printStackTrace();
            datosArray = new JSONArray();
        }
    }

    private void guardarDatos() {
        sessionStore.saveDatosExperimentalesJson(asignacionId, datosArray.toString());
    }

    private void renderDatos() {
        layoutDatosContainer.removeAllViews();

        if (datosArray.length() == 0) {
            tvEstadoDatos.setText("Aún no has registrado datos experimentales.");
            return;
        }

        tvEstadoDatos.setText("Datos registrados: " + datosArray.length());

        for (int i = 0; i < datosArray.length(); i++) {
            JSONObject item = datosArray.optJSONObject(i);
            if (item == null) continue;

            String titulo = item.optString("titulo", "Dato experimental");
            String descripcion = item.optString("descripcion", "");

            View card = getLayoutInflater().inflate(
                    R.layout.item_dato_experimental,
                    layoutDatosContainer,
                    false
            );

            TextView tvTitulo = card.findViewById(R.id.tvTituloDatoExperimental);
            TextView tvDescripcion = card.findViewById(R.id.tvDescripcionDatoExperimental);
            Button btnEditar = card.findViewById(R.id.btnEditarDatoExperimental);
            Button btnEliminar = card.findViewById(R.id.btnEliminarDatoExperimental);

            tvTitulo.setText(titulo);
            tvDescripcion.setText(descripcion);

            int index = i;

            btnEditar.setOnClickListener(v -> {
                JSONObject current = datosArray.optJSONObject(index);
                if (current == null) return;

                mostrarDialogoDato(
                        index,
                        current.optString("titulo", ""),
                        current.optString("descripcion", "")
                );
            });

            btnEliminar.setOnClickListener(v -> {
                datosArray.remove(index);
                guardarDatos();
                renderDatos();
            });

            layoutDatosContainer.addView(card);
        }
    }

    private void mostrarDialogoDato(int index, String tituloActual, String descripcionActual) {
        View view = getLayoutInflater().inflate(R.layout.dialog_dato_experimental, null, false);

        EditText etTitulo = view.findViewById(R.id.etTituloDatoExperimental);
        EditText etDescripcion = view.findViewById(R.id.etDescripcionDatoExperimental);

        if (tituloActual != null) etTitulo.setText(tituloActual);
        if (descripcionActual != null) etDescripcion.setText(descripcionActual);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(index >= 0 ? "Editar dato experimental" : "Agregar dato experimental")
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positive.setOnClickListener(v -> {
                String titulo = etTitulo.getText().toString().trim();
                String descripcion = etDescripcion.getText().toString().trim();

                if (titulo.isEmpty()) {
                    etTitulo.setError("Ingresa un título");
                    return;
                }

                if (descripcion.isEmpty()) {
                    etDescripcion.setError("Ingresa una descripción");
                    return;
                }

                try {
                    JSONObject item = new JSONObject();
                    item.put("titulo", titulo);
                    item.put("descripcion", descripcion);

                    if (index >= 0) {
                        datosArray.put(index, item);
                    } else {
                        datosArray.put(item);
                    }

                    guardarDatos();
                    renderDatos();
                    dialog.dismiss();

                } catch (Exception e) {
                    e.printStackTrace();
                    etTitulo.setError("No se pudo guardar");
                }
            });
        });

        dialog.show();
    }

    private void completarPaso() {
        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }
}