package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
import com.marcos.fisikappmovil.ui.common.StepCompletionOverlay;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PracticaExperimental extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnContinuar;

    private LinearLayout mainContainer;
    private LinearLayout layoutPracticeInfoContainer;
    private LinearLayout layoutProcedureContainer;
    private LinearLayout layoutExpectedInputsContainer;

    private JSONObject experimentalPracticeStep;
    private JSONObject exerciseObject;
    private JSONObject expectedInputsData = new JSONObject();

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

        cargarEvidenciasGuardadas();
        cargarExpectedInputsGuardados();
        cargarPracticaExperimentalDesdeJson();

        renderEvidencias();

        initListeners();
        configurarModoPantalla();
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

        mainContainer = findViewById(R.id.mainContainer);
        layoutPracticeInfoContainer = findViewById(R.id.layoutPracticeInfoContainer);
        layoutProcedureContainer = findViewById(R.id.layoutProcedureContainer);
        layoutExpectedInputsContainer = findViewById(R.id.layoutExpectedInputsContainer);

        //layoutMockPracticeContent = findViewById(R.id.layoutMockPracticeContent);

        layoutAgregarEvidencia = findViewById(R.id.layoutAgregarEvidencia);
        layoutEvidenciasContainer = findViewById(R.id.layoutEvidenciasContainer);
        tvEstadoEvidencias = findViewById(R.id.tvEstadoEvidencias);
    }

    private void initListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (layoutAgregarEvidencia != null) {
            layoutAgregarEvidencia.setOnClickListener(v -> {
                if (modoSoloLectura()){
                    return;
                }
                mostrarOpcionesEvidencia();
            });
        }

        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(v -> completarPaso());
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

    private boolean validarExpectedInputs() {
        if (exerciseObject == null) {
            return true;
        }

        JSONArray inputsArray = exerciseObject.optJSONArray("expected_inputs");

        if (inputsArray == null || inputsArray.length() == 0) {
            return true;
        }

        for (int i = 0; i < inputsArray.length(); i++) {
            JSONObject input = inputsArray.optJSONObject(i);
            if (input == null) continue;

            String id = input.optString("id", "");
            String type = input.optString("type", "");
            String label = input.optString("label", id);
            boolean required = input.optBoolean("required", false);

            if (!required) {
                continue;
            }

            if ("TEXT".equalsIgnoreCase(type)) {
                EditText editText = layoutExpectedInputsContainer.findViewWithTag(id);

                if (editText == null || editText.getText().toString().trim().isEmpty()) {
                    if (editText != null) {
                        editText.setError("Campo obligatorio");
                        editText.requestFocus();
                    }

                    android.widget.Toast.makeText(
                            this,
                            "Completa el campo: " + label,
                            android.widget.Toast.LENGTH_SHORT
                    ).show();

                    return false;
                }
            }

            if ("FILES".equalsIgnoreCase(type)) {
                if (evidenciasArray == null || evidenciasArray.length() == 0) {
                    android.widget.Toast.makeText(
                            this,
                            "Agrega al menos una evidencia.",
                            android.widget.Toast.LENGTH_SHORT
                    ).show();

                    return false;
                }
            }
        }

        return true;
    }
    private void completarPaso() {
        if (modoSoloLectura()) {
            finish();
            return;
        }

        if (!validarExpectedInputs()) {
            return;
        }

        guardarExpectedInputs();
        guardarEvidencias();

        Intent data = new Intent();
        data.putExtra(PasosLaboratorio.EXTRA_ORDEN_PASO, ordenPaso);

        StepCompletionOverlay.show(this, () -> {
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void cargarPracticaExperimentalDesdeJson() {
        android.util.Log.d(
                "PRACTICA_JSON",
                "asignacionId=" + asignacionId
                        + " | laboratorioId=" + laboratorioId
                        + " | grupoId=" + grupoId
                        + " | ordenPaso=" + ordenPaso
        );

        String json = sessionStore.getMobileResourceJson(asignacionId);

        android.util.Log.d(
                "PRACTICA_JSON",
                "mobileResourceJson existe=" + (json != null && !json.trim().isEmpty())
        );

        if (json == null || json.trim().isEmpty()) {
            renderErrorPractica("No se encontró información guardada del laboratorio.");
            return;
        }

        try {
            JSONObject root = new JSONObject(json);
            JSONArray steps = root.optJSONArray("steps");

            android.util.Log.d(
                    "PRACTICA_JSON",
                    "steps length=" + (steps == null ? -1 : steps.length())
            );

            if (steps == null || steps.length() == 0) {
                renderErrorPractica("Este laboratorio no tiene pasos configurados.");
                return;
            }

            experimentalPracticeStep = findExperimentalPracticeStep(steps);

            android.util.Log.d(
                    "PRACTICA_JSON",
                    "experimentalPracticeStep encontrado=" + (experimentalPracticeStep != null)
            );

            if (experimentalPracticeStep == null) {
                renderErrorPractica("No se encontró la práctica experimental en este laboratorio.");
                return;
            }

            exerciseObject = experimentalPracticeStep.optJSONObject("exercise");

            android.util.Log.d(
                    "PRACTICA_JSON",
                    "exercise encontrado=" + (exerciseObject != null)
            );

            if (exerciseObject == null) {
                renderErrorPractica("La práctica experimental no tiene información del ejercicio.");
                return;
            }

            renderPracticeInfo(exerciseObject);
            renderProcedure(exerciseObject.optJSONArray("procedure"));
            renderExpectedInputs(exerciseObject.optJSONArray("expected_inputs"));

        } catch (Exception e) {
            android.util.Log.e("PRACTICA_JSON", "Error leyendo práctica experimental", e);
            renderErrorPractica("No se pudo leer la práctica experimental: " + e.getMessage());
        }
    }

    private JSONObject findExperimentalPracticeStep(JSONArray steps) {
        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.optJSONObject(i);
            if (step == null) continue;

            String type = step.optString("type", "");
            int order = step.optInt("order", -1);

            if ("EXPERIMENTAL_PRACTICE".equalsIgnoreCase(type)) {
                return step;
            }

            if (ordenPaso > 0 && order == ordenPaso) {
                return step;
            }
        }

        return null;
    }

    private void renderPracticeInfo(JSONObject exercise) {
        layoutPracticeInfoContainer.removeAllViews();

        addSectionTitle(layoutPracticeInfoContainer, exercise.optString("name", "Ejercicio experimental"));

        addInfoCard(
                layoutPracticeInfoContainer,
                "Objetivo",
                exercise.optString("objective", "Sin objetivo disponible.")
        );

        addInfoCard(
                layoutPracticeInfoContainer,
                "Descripción",
                exercise.optString("description", "Sin descripción disponible.")
        );

        addInfoCard(
                layoutPracticeInfoContainer,
                "Materiales",
                exercise.optString("materials", "Sin materiales registrados.")
        );

        addInfoCard(
                layoutPracticeInfoContainer,
                "Cálculos",
                exercise.optString("calculations", "Sin cálculos registrados.")
        );
    }

    private void renderProcedure(JSONArray procedureArray) {
        layoutProcedureContainer.removeAllViews();

        addSectionTitle(layoutProcedureContainer, "Procedimiento paso a paso");

        if (procedureArray == null || procedureArray.length() == 0) {
            addInfoCard(layoutProcedureContainer, "Procedimiento", "No hay procedimiento disponible.");
            return;
        }

        for (int i = 0; i < procedureArray.length(); i++) {
            JSONObject item = procedureArray.optJSONObject(i);
            if (item == null) continue;

            int number = item.optInt("number", i + 1);
            String description = item.optString("description", "");

            addProcedureItem(layoutProcedureContainer, number, description);
        }
    }

    private void renderExpectedInputs(JSONArray inputsArray) {
        layoutExpectedInputsContainer.removeAllViews();

        addSectionTitle(layoutExpectedInputsContainer, "Registro de la práctica");

        if (inputsArray == null || inputsArray.length() == 0) {
            addInfoCard(layoutExpectedInputsContainer, "Campos", "No hay campos de registro configurados.");
            return;
        }

        for (int i = 0; i < inputsArray.length(); i++) {
            JSONObject input = inputsArray.optJSONObject(i);
            if (input == null) continue;

            String id = input.optString("id", "");
            String type = input.optString("type", "");
            String label = input.optString("label", id);
            boolean required = input.optBoolean("required", false);

            if ("TEXT".equalsIgnoreCase(type)) {
                addTextInputField(id, label, required);
            }

            if ("FILES".equalsIgnoreCase(type)) {
                // Ya se maneja con layoutAgregarEvidencia.
                // Opcional: podríamos actualizar el texto del bloque de evidencias con este label.
            }
        }
    }

    private void addTextInputField(String id, String label, boolean required) {
        TextView tvLabel = new TextView(this);
        tvLabel.setText(required ? label + " *" : label);
        tvLabel.setTextColor(getResources().getColor(R.color.colorPrimario));
        tvLabel.setTextSize(14);
        tvLabel.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, dpToPx(12), 0, dpToPx(6));
        tvLabel.setLayoutParams(labelParams);

        EditText editText = new EditText(this);
        editText.setTag(id);
        editText.setMinLines(3);
        editText.setGravity(android.view.Gravity.TOP);
        editText.setHint("Escribe " + label.toLowerCase(Locale.ROOT));
        editText.setTextColor(android.graphics.Color.parseColor("#334155"));
        editText.setTextSize(14);
        editText.setBackgroundResource(R.drawable.input_recuperrar);
        editText.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

        String savedValue = expectedInputsData.optString(id, "");
        editText.setText(savedValue);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editText.setLayoutParams(inputParams);

        layoutExpectedInputsContainer.addView(tvLabel);
        layoutExpectedInputsContainer.addView(editText);
    }

    private void cargarExpectedInputsGuardados() {
        String json = sessionStore.getPracticaExperimentalJson(asignacionId);

        if (json == null || json.trim().isEmpty()) {
            expectedInputsData = new JSONObject();
            return;
        }

        try {
            expectedInputsData = new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
            expectedInputsData = new JSONObject();
        }
    }

    private void guardarExpectedInputs() {
        try {
            JSONObject data = new JSONObject();

            recogerInputsDesdeContenedor(layoutExpectedInputsContainer, data);

            data.put("updatedAt", obtenerFechaActual());

            sessionStore.savePracticaExperimentalJson(asignacionId, data.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recogerInputsDesdeContenedor(View view, JSONObject data) throws Exception {
        if (view instanceof EditText) {
            Object tag = view.getTag();

            if (tag != null) {
                String id = tag.toString();
                String value = ((EditText) view).getText().toString().trim();
                data.put(id, value);
            }

            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                recogerInputsDesdeContenedor(group.getChildAt(i), data);
            }
        }
    }

    // Helpers

    private void addSectionTitle(LinearLayout parent, String text) {
        TextView view = new TextView(this);
        view.setText(text == null ? "" : text);
        view.setTextColor(getResources().getColor(R.color.colorPrimario));
        view.setTextSize(16);
        view.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(8), 0, dpToPx(8));
        view.setLayoutParams(params);

        parent.addView(view);
    }

    private void addInfoCard(LinearLayout parent, String title, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
        card.setBackgroundResource(R.drawable.bg_card_rounded);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(8), 0, dpToPx(8));
        card.setLayoutParams(params);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title == null ? "" : title);
        tvTitle.setTextColor(getResources().getColor(R.color.colorPrimario));
        tvTitle.setTextSize(14);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvValue = new TextView(this);
        tvValue.setText(value == null || value.trim().isEmpty() ? "Sin información disponible." : value);
        tvValue.setTextColor(android.graphics.Color.parseColor("#475569"));
        tvValue.setTextSize(13);
        tvValue.setLineSpacing(4f, 1.1f);
        tvValue.setPadding(0, dpToPx(6), 0, 0);

        card.addView(tvTitle);
        card.addView(tvValue);

        parent.addView(card);
    }

    private void addProcedureItem(LinearLayout parent, int number, String description) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dpToPx(8), 0, dpToPx(8));

        TextView tvNumber = new TextView(this);
        tvNumber.setText(String.valueOf(number));
        tvNumber.setGravity(android.view.Gravity.CENTER);
        tvNumber.setTextColor(android.graphics.Color.WHITE);
        tvNumber.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNumber.setBackgroundColor(android.graphics.Color.parseColor("#FACC15"));

        LinearLayout.LayoutParams numberParams = new LinearLayout.LayoutParams(
                dpToPx(26),
                dpToPx(26)
        );
        numberParams.setMargins(0, 0, dpToPx(12), 0);
        tvNumber.setLayoutParams(numberParams);

        TextView tvDescription = new TextView(this);
        tvDescription.setText(description == null ? "" : description);
        tvDescription.setTextColor(android.graphics.Color.parseColor("#334155"));
        tvDescription.setTextSize(13);

        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        tvDescription.setLayoutParams(descParams);

        row.addView(tvNumber);
        row.addView(tvDescription);

        parent.addView(row);
    }

    private void renderErrorPractica(String message) {
        if (layoutPracticeInfoContainer != null) {
            layoutPracticeInfoContainer.removeAllViews();
            addInfoCard(layoutPracticeInfoContainer, "No se pudo cargar la práctica", message);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void configurarModoPantalla() {
        if (modoSoloLectura()) {
            btnContinuar.setText("Regresar");

            setEditableRecursive(layoutExpectedInputsContainer, false);

            if (layoutAgregarEvidencia != null) {
                layoutAgregarEvidencia.setEnabled(false);
                layoutAgregarEvidencia.setAlpha(0.45f);
                layoutAgregarEvidencia.setVisibility(View.GONE);
            }

        } else {
            btnContinuar.setText("Guardar y continuar");

            setEditableRecursive(layoutExpectedInputsContainer, true);

            if (layoutAgregarEvidencia != null) {
                layoutAgregarEvidencia.setEnabled(true);
                layoutAgregarEvidencia.setAlpha(1f);
                layoutAgregarEvidencia.setVisibility(View.VISIBLE);
            }
        }
    }

    // Helper
    private void setEditableRecursive(View view, boolean enabled) {
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            editText.setEnabled(enabled);
            editText.setFocusable(enabled);
            editText.setFocusableInTouchMode(enabled);
            editText.setCursorVisible(enabled);
            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                setEditableRecursive(group.getChildAt(i), enabled);
            }
        }
    }

    // Helper de estado
    private boolean pasoYaCompletado() {
        return asignacionId > 0
                && ordenPaso > 0
                && sessionStore.estaPasoCompletado(asignacionId, ordenPaso);
    }

    private boolean laboratorioYaEntregado() {
        return asignacionId > 0
                && sessionStore.isEntregaEnviada(asignacionId);
    }

    private boolean modoSoloLectura() {
        return pasoYaCompletado() || laboratorioYaEntregado();
    }

}