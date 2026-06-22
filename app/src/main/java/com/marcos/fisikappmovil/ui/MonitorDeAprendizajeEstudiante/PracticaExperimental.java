package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.session.LaboratorioSessionStore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PracticaExperimental extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnContinuar;

    private LinearLayout layoutPaso1, layoutPaso2, layoutPaso3, layoutPaso4, layoutPaso5, layoutPaso6, layoutPaso7;
    private ImageView imgPaso1, imgPaso2, imgPaso3, imgPaso4, imgPaso5, imgPaso6, imgPaso7;

    private LinearLayout layoutAgregarEvidencia;
    private LinearLayout layoutEvidenciasContainer;
    private TextView tvEstadoEvidencias;

    private LaboratorioSessionStore sessionStore;

    private int asignacionId = -1;
    private int laboratorioId = -1;
    private int grupoId = -1;
    private int ordenPaso = -1;

    private JSONArray evidenciasArray = new JSONArray();

    private final ActivityResultLauncher<String[]> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    procesarUriSeleccionada(uri, "IMAGEN");
                }
            });

    private final ActivityResultLauncher<String[]> seleccionarDocumentoLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    procesarUriSeleccionada(uri, "DOCUMENTO");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practica_experimental);

        sessionStore = new LaboratorioSessionStore(this);

        readExtras();
        initViews();
        initAccordionViews();
        setupStepListeners();

        cargarEvidenciasGuardadas();
        renderEvidencias();

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
        btnBack = findViewById(R.id.btnBack);
        btnContinuar = findViewById(R.id.btnContinuar);

        layoutAgregarEvidencia = findViewById(R.id.layoutAgregarEvidencia);
        layoutEvidenciasContainer = findViewById(R.id.layoutEvidenciasContainer);
        tvEstadoEvidencias = findViewById(R.id.tvEstadoEvidencias);
    }

    private void initListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (layoutAgregarEvidencia != null) {
            layoutAgregarEvidencia.setOnClickListener(v -> mostrarOpcionesEvidencia());
        }

        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(v -> completarPaso());
        }
    }

    private void initAccordionViews() {
        layoutPaso1 = findViewById(R.id.layoutPaso1);
        layoutPaso2 = findViewById(R.id.layoutPaso2);
        layoutPaso3 = findViewById(R.id.layoutPaso3);
        layoutPaso4 = findViewById(R.id.layoutPaso4);
        layoutPaso5 = findViewById(R.id.layoutPaso5);
        layoutPaso6 = findViewById(R.id.layoutPaso6);
        layoutPaso7 = findViewById(R.id.layoutPaso7);

        imgPaso1 = findViewById(R.id.imgPaso1);
        imgPaso2 = findViewById(R.id.imgPaso2);
        imgPaso3 = findViewById(R.id.imgPaso3);
        imgPaso4 = findViewById(R.id.imgPaso4);
        imgPaso5 = findViewById(R.id.imgPaso5);
        imgPaso6 = findViewById(R.id.imgPaso6);
        imgPaso7 = findViewById(R.id.imgPaso7);
    }

    private void setupStepListeners() {
        if (layoutPaso1 != null) layoutPaso1.setOnClickListener(v -> toggleStep(imgPaso1, (ViewGroup) layoutPaso1.getParent()));
        if (layoutPaso2 != null) layoutPaso2.setOnClickListener(v -> toggleStep(imgPaso2, (ViewGroup) layoutPaso2.getParent()));
        if (layoutPaso3 != null) layoutPaso3.setOnClickListener(v -> toggleStep(imgPaso3, (ViewGroup) layoutPaso3.getParent()));
        if (layoutPaso4 != null) layoutPaso4.setOnClickListener(v -> toggleStep(imgPaso4, (ViewGroup) layoutPaso4.getParent()));
        if (layoutPaso5 != null) layoutPaso5.setOnClickListener(v -> toggleStep(imgPaso5, (ViewGroup) layoutPaso5.getParent()));
        if (layoutPaso6 != null) layoutPaso6.setOnClickListener(v -> toggleStep(imgPaso6, (ViewGroup) layoutPaso6.getParent()));
        if (layoutPaso7 != null) layoutPaso7.setOnClickListener(v -> toggleStep(imgPaso7, (ViewGroup) layoutPaso7.getParent()));
    }

    private void toggleStep(ImageView imageView, ViewGroup parent) {
        if (imageView == null) return;

        TransitionManager.beginDelayedTransition(parent);

        if (imageView.getVisibility() == View.GONE) {
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    private void mostrarOpcionesEvidencia() {
        String[] opciones = {
                "Seleccionar imagen",
                "Seleccionar documento"
        };

        new AlertDialog.Builder(this)
                .setTitle("Agregar evidencia")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        seleccionarImagenLauncher.launch(new String[]{"image/*"});
                    } else {
                        seleccionarDocumentoLauncher.launch(new String[]{
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "text/plain",
                                "image/*"
                        });
                    }
                })
                .show();
    }

    private void procesarUriSeleccionada(Uri uri, String tipo) {
        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        String nombre = obtenerNombreArchivo(uri);

        mostrarDialogoDescripcion(-1, tipo, nombre, uri.toString(), "");
    }

    private void mostrarDialogoDescripcion(
            int index,
            String tipo,
            String nombre,
            String uri,
            String descripcionActual
    ) {
        View view = getLayoutInflater().inflate(R.layout.dialog_descripcion_evidencia, null, false);

        TextView tvNombreArchivoDialog = view.findViewById(R.id.tvNombreArchivoDialog);
        EditText etDescripcion = view.findViewById(R.id.etDescripcionEvidencia);

        tvNombreArchivoDialog.setText(nombre);

        if (descripcionActual != null) {
            etDescripcion.setText(descripcionActual);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(index >= 0 ? "Editar evidencia" : "Agregar evidencia")
                .setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positive.setOnClickListener(v -> {
                String descripcion = etDescripcion.getText().toString().trim();

                try {
                    JSONObject item = new JSONObject();

                    item.put("id", index >= 0
                            ? evidenciasArray.optJSONObject(index).optString("id")
                            : "EV-" + System.currentTimeMillis()
                    );

                    item.put("tipo", tipo);
                    item.put("nombre", nombre);
                    item.put("uri", uri);
                    item.put("descripcion", descripcion);
                    item.put("createdAt", obtenerFechaActual());

                    if (index >= 0) {
                        evidenciasArray.put(index, item);
                    } else {
                        evidenciasArray.put(item);
                    }

                    guardarEvidencias();
                    renderEvidencias();

                    dialog.dismiss();

                } catch (Exception e) {
                    e.printStackTrace();
                    etDescripcion.setError("No se pudo guardar la evidencia");
                }
            });
        });

        dialog.show();
    }

    private void cargarEvidenciasGuardadas() {
        String json = sessionStore.getEvidenciasJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            evidenciasArray = new JSONArray();
            return;
        }

        try {
            evidenciasArray = new JSONArray(json);
        } catch (Exception e) {
            e.printStackTrace();
            evidenciasArray = new JSONArray();
        }
    }

    private void guardarEvidencias() {
        sessionStore.saveEvidenciasJson(asignacionId, evidenciasArray.toString());
    }

    private void renderEvidencias() {
        layoutEvidenciasContainer.removeAllViews();

        if (evidenciasArray.length() == 0) {
            tvEstadoEvidencias.setText("Aún no has agregado evidencias.");
            return;
        }

        tvEstadoEvidencias.setText("Evidencias agregadas: " + evidenciasArray.length());

        for (int i = 0; i < evidenciasArray.length(); i++) {
            JSONObject item = evidenciasArray.optJSONObject(i);
            if (item == null) continue;

            String tipo = item.optString("tipo", "EVIDENCIA");
            String nombre = item.optString("nombre", "Archivo");
            String uri = item.optString("uri", "");
            String descripcion = item.optString("descripcion", "Sin descripción");

            View card = getLayoutInflater().inflate(
                    R.layout.item_evidencia_practica,
                    layoutEvidenciasContainer,
                    false
            );

            ImageView imgIcono = card.findViewById(R.id.imgIconoEvidencia);
            TextView tvNombre = card.findViewById(R.id.tvNombreEvidencia);
            TextView tvTipo = card.findViewById(R.id.tvTipoEvidencia);
            TextView tvDescripcion = card.findViewById(R.id.tvDescripcionEvidencia);
            Button btnEditar = card.findViewById(R.id.btnEditarEvidencia);
            Button btnEliminar = card.findViewById(R.id.btnEliminarEvidencia);

            tvNombre.setText(nombre);
            tvTipo.setText(tipo);
            tvDescripcion.setText(descripcion == null || descripcion.trim().isEmpty()
                    ? "Sin descripción"
                    : descripcion);

            if ("IMAGEN".equalsIgnoreCase(tipo)) {
                imgIcono.setImageResource(android.R.drawable.ic_menu_gallery);
            } else {
                imgIcono.setImageResource(android.R.drawable.ic_menu_upload);
            }

            int index = i;

            btnEditar.setOnClickListener(v -> {
                JSONObject current = evidenciasArray.optJSONObject(index);
                if (current == null) return;

                mostrarDialogoDescripcion(
                        index,
                        current.optString("tipo", "EVIDENCIA"),
                        current.optString("nombre", "Archivo"),
                        current.optString("uri", ""),
                        current.optString("descripcion", "")
                );
            });

            btnEliminar.setOnClickListener(v -> {
                evidenciasArray.remove(index);
                guardarEvidencias();
                renderEvidencias();
            });

            layoutEvidenciasContainer.addView(card);
        }
    }

    private String obtenerNombreArchivo(Uri uri) {
        String result = null;

        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                try {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        result = cursor.getString(nameIndex);
                    }

                } finally {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result == null || result.trim().isEmpty()) {
            result = "evidencia_" + System.currentTimeMillis();
        }

        return result;
    }

    private String obtenerFechaActual() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                .format(new Date());
    }

    private void completarPaso() {
        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);
        setResult(RESULT_OK, data);
        finish();
    }
}