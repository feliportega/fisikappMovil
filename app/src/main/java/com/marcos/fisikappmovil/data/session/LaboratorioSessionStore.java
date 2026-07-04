package com.marcos.fisikappmovil.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.marcos.fisikappmovil.model.LaboratorioPasoItem;

public class LaboratorioSessionStore {

    private static final String PREFS_NAME = "laboratorio_session_store";

    private final SharedPreferences prefs;

    public LaboratorioSessionStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private String keyPaso(int asignacionId, int ordenPaso) {
        return "asignacion_" + asignacionId + "_paso_" + ordenPaso;
    }

    private String keyUnityResult(int asignacionId) {
        return "asignacion_" + asignacionId + "_unity_result_json";
    }

    private String keyUnityContextByRunId(String runId) {
        return "unity_context_run_" + runId;
    }

    private String keyUnityContextByRequestId(String requestId) {
        return "unity_context_request_" + requestId;
    }

    private String keyEntregaEnviada(int asignacionId) {
        return "asignacion_" + asignacionId + "_entrega_enviada";
    }

    public String getEstadoPaso(int asignacionId, int ordenPaso) {
        if (ordenPaso == 1) {
            return prefs.getString(
                    keyPaso(asignacionId, ordenPaso),
                    LaboratorioPasoItem.ESTADO_PENDIENTE
            );
        }

        return prefs.getString(
                keyPaso(asignacionId, ordenPaso),
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        );
    }

    public void setEstadoPaso(int asignacionId, int ordenPaso, String estado) {
        prefs.edit()
                .putString(keyPaso(asignacionId, ordenPaso), estado)
                .apply();
    }

    public void completarPasoYDesbloquearSiguiente(int asignacionId, int ordenPaso) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(
                keyPaso(asignacionId, ordenPaso),
                LaboratorioPasoItem.ESTADO_COMPLETADO
        );

        editor.putString(
                keyPaso(asignacionId, ordenPaso + 1),
                LaboratorioPasoItem.ESTADO_PENDIENTE
        );

        editor.apply();
    }

    public boolean estaPasoCompletado(int asignacionId, int ordenPaso) {
        return LaboratorioPasoItem.ESTADO_COMPLETADO.equalsIgnoreCase(
                getEstadoPaso(asignacionId, ordenPaso)
        );
    }

    public void saveUnityResultJson(int asignacionId, String json) {
        prefs.edit()
                .putString(keyUnityResult(asignacionId), json)
                .apply();
    }

    public String getUnityResultJson(int asignacionId) {
        return prefs.getString(keyUnityResult(asignacionId), null);
    }

    public boolean hasUnityResult(int asignacionId) {
        String json = getUnityResultJson(asignacionId);
        return json != null && !json.trim().isEmpty();
    }

    public void marcarEntregaEnviada(int asignacionId) {
        prefs.edit()
                .putBoolean(keyEntregaEnviada(asignacionId), true)
                .apply();
    }

    public boolean isEntregaEnviada(int asignacionId) {
        return prefs.getBoolean(keyEntregaEnviada(asignacionId), false);
    }

    private String keyPreguntasJson(int asignacionId) {
        return "asignacion_" + asignacionId + "_preguntas_json";
    }

    private String keyDatosExperimentalesJson(int asignacionId) {
        return "asignacion_" + asignacionId + "_datos_experimentales_json";
    }

    private String keyEvidenciasJson(int asignacionId) {
        return "asignacion_" + asignacionId + "_evidencias_json";
    }

    private String keyComparacionTexto(int asignacionId) {
        return "asignacion_" + asignacionId + "_comparacion_texto";
    }

    private String keyConclusionesTexto(int asignacionId) {
        return "asignacion_" + asignacionId + "_conclusiones_texto";
    }

    public void savePreguntasJson(int asignacionId, String json) {
        prefs.edit().putString(keyPreguntasJson(asignacionId), json).apply();
    }

    public String getPreguntasJson(int asignacionId) {
        return prefs.getString(keyPreguntasJson(asignacionId), null);
    }

    public void saveDatosExperimentalesJson(int assignmentId, String json) {
        prefs.edit()
                .putString("datos_experimentales_json_" + assignmentId, json)
                .apply();
    }

    public String getDatosExperimentalesJson(int assignmentId) {
        return prefs.getString("datos_experimentales_json_" + assignmentId, null);
    }

    public void savePracticaExperimentalJson(int assignmentId, String json) {
        prefs.edit()
                .putString("practica_experimental_json_" + assignmentId, json)
                .apply();
    }

    public String getPracticaExperimentalJson(int assignmentId) {
        return prefs.getString("practica_experimental_json_" + assignmentId, null);
    }

    public void saveEvidenciasJson(int asignacionId, String json) {
        prefs.edit().putString(keyEvidenciasJson(asignacionId), json).apply();
    }

    public String getEvidenciasJson(int asignacionId) {
        return prefs.getString(keyEvidenciasJson(asignacionId), null);
    }

    public void saveComparacionTexto(int asignacionId, String texto) {
        prefs.edit().putString(keyComparacionTexto(asignacionId), texto).apply();
    }

    public String getComparacionTexto(int asignacionId) {
        return prefs.getString(keyComparacionTexto(asignacionId), "");
    }

    public void saveConclusionesTexto(int asignacionId, String texto) {
        prefs.edit().putString(keyConclusionesTexto(asignacionId), texto).apply();
    }

    public String getConclusionesTexto(int asignacionId) {
        return prefs.getString(keyConclusionesTexto(asignacionId), "");
    }

    public void saveUnityLaunchContext(
            String runId,
            String requestId,
            int asignacionId,
            int laboratorioId,
            int grupoId,
            int ordenPaso
    ) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();

            json.put("runId", runId);
            json.put("requestId", requestId);
            json.put("asignacionId", asignacionId);
            json.put("laboratorioId", laboratorioId);
            json.put("grupoId", grupoId);
            json.put("ordenPaso", ordenPaso);

            SharedPreferences.Editor editor = prefs.edit();

            if (runId != null && !runId.trim().isEmpty()) {
                editor.putString(keyUnityContextByRunId(runId), json.toString());
            }

            if (requestId != null && !requestId.trim().isEmpty()) {
                editor.putString(keyUnityContextByRequestId(requestId), json.toString());
            }

            editor.apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public org.json.JSONObject getUnityLaunchContext(String runId, String requestId) {
        try {
            String json = null;

            if (runId != null && !runId.trim().isEmpty()) {
                json = prefs.getString(keyUnityContextByRunId(runId), null);
            }

            if ((json == null || json.trim().isEmpty())
                    && requestId != null
                    && !requestId.trim().isEmpty()) {
                json = prefs.getString(keyUnityContextByRequestId(requestId), null);
            }

            if (json == null || json.trim().isEmpty()) {
                return null;
            }

            return new org.json.JSONObject(json);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void resetLaboratorio(int asignacionId) {
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 1; i <= 12; i++) {
            editor.remove(keyPaso(asignacionId, i));
        }

        editor.remove(keyUnityResult(asignacionId));
        editor.remove(keyEntregaEnviada(asignacionId));
        editor.remove(keyPreguntasJson(asignacionId));
        editor.remove(keyDatosExperimentalesJson(asignacionId));
        editor.remove(keyEvidenciasJson(asignacionId));
        editor.remove(keyComparacionTexto(asignacionId));
        editor.remove(keyConclusionesTexto(asignacionId));

        editor.remove("comparacion_resultados_json_" + asignacionId);
        editor.remove("informe_laboratorio_json_" + asignacionId);
        editor.remove("unity_started_at_" + asignacionId);
        editor.remove("unity_result_sent_" + asignacionId);
        editor.remove("unity_result_send_error_" + asignacionId);

        editor.apply();
    }

    public void saveMobileResourceJson(int assignmentId, String json) {
        prefs.edit()
                .putString("mobile_resource_json_" + assignmentId, json)
                .apply();
    }

    public String getMobileResourceJson(int assignmentId) {
        return prefs.getString("mobile_resource_json_" + assignmentId, null);
    }

    public boolean hasMobileResourceJson(int assignmentId) {
        String json = getMobileResourceJson(assignmentId);
        return json != null && !json.trim().isEmpty();
    }

    public void clearMobileResourceJson(int assignmentId) {
        prefs.edit()
                .remove("mobile_resource_json_" + assignmentId)
                .apply();
    }

    public void saveComparacionResultadosJson(int assignmentId, String json) {
        prefs.edit()
                .putString("comparacion_resultados_json_" + assignmentId, json)
                .apply();
    }

    public String getComparacionResultadosJson(int assignmentId) {
        return prefs.getString("comparacion_resultados_json_" + assignmentId, null);
    }

    public void saveInformeLaboratorioJson(int assignmentId, String json) {
        prefs.edit()
                .putString("informe_laboratorio_json_" + assignmentId, json)
                .apply();
    }

    public String getInformeLaboratorioJson(int assignmentId) {
        return prefs.getString("informe_laboratorio_json_" + assignmentId, null);
    }

    public void saveUnityStartedAt(int assignmentId, String startedAt) {
        prefs.edit()
                .putString("unity_started_at_" + assignmentId, startedAt)
                .apply();
    }

    public String getUnityStartedAt(int assignmentId) {
        return prefs.getString("unity_started_at_" + assignmentId, null);
    }

    public void marcarUnityResultEnviado(int assignmentId) {
        prefs.edit()
                .putBoolean("unity_result_sent_" + assignmentId, true)
                .apply();
    }

    public boolean isUnityResultEnviado(int assignmentId) {
        return prefs.getBoolean("unity_result_sent_" + assignmentId, false);
    }

    public void saveUnityResultSendError(int assignmentId, String error) {
        prefs.edit()
                .putString("unity_result_send_error_" + assignmentId, error)
                .apply();
    }

    public String getUnityResultSendError(int assignmentId) {
        return prefs.getString("unity_result_send_error_" + assignmentId, null);
    }

    public void saveSimulationConfigJson(int simulationId, String json) {
        prefs.edit()
                .putString("simulation_config_json_" + simulationId, json)
                .apply();
    }

    public String getSimulationConfigJson(int simulationId) {
        return prefs.getString("simulation_config_json_" + simulationId, null);
    }

    public boolean hasSimulationConfigJson(int simulationId) {
        String json = getSimulationConfigJson(simulationId);
        return json != null && !json.trim().isEmpty();
    }

    public void clearSimulationConfigJson(int simulationId) {
        prefs.edit()
                .remove("simulation_config_json_" + simulationId)
                .apply();
    }

}