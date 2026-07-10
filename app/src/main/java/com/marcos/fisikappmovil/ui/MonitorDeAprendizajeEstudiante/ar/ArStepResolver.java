package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.ar;

import org.json.JSONArray;
import org.json.JSONObject;

public class ArStepResolver {

    private ArStepResolver() {
        // Utility class
    }

    public static ArStepConfig resolve(
            String mobileResourceJson,
            String stepId,
            int ordenPaso
    ) {
        if (mobileResourceJson == null || mobileResourceJson.trim().isEmpty()) {
            return null;
        }

        try {
            JSONObject root = new JSONObject(mobileResourceJson);
            JSONArray steps = root.optJSONArray("steps");

            if (steps == null || steps.length() == 0) {
                return null;
            }

            JSONObject simulationStep = findSimulationStep(steps, stepId, ordenPaso);

            if (simulationStep == null) {
                return null;
            }

            JSONObject simulationRef = simulationStep.optJSONObject("simulation_ref");

            if (simulationRef == null) {
                return null;
            }

            int arId = simulationRef.optInt("ar_id", -1);

            String labKey = simulationRef.optString("lab_key", "");
            String unitySceneName = simulationRef.optString("unity_scene_name", "");
            String displayName = simulationRef.optString("display_name", "");

            return new ArStepConfig(
                    arId,
                    safe(labKey),
                    safe(unitySceneName),
                    safe(displayName)
            );

        } catch (Exception e) {
            android.util.Log.e("AR_STEP_RESOLVER", "Error resolviendo step AR", e);
            return null;
        }
    }

    private static JSONObject findSimulationStep(
            JSONArray steps,
            String stepId,
            int ordenPaso
    ) {
        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.optJSONObject(i);

            if (step == null) {
                continue;
            }

            String id = step.optString("id", "");
            String type = step.optString("type", "");
            int order = step.optInt("order", -1);

            if (stepId != null && stepId.equals(id)) {
                return step;
            }

            if ("SIMULATION_AR".equalsIgnoreCase(type)) {
                return step;
            }

            if (ordenPaso > 0 && ordenPaso == order) {
                return step;
            }
        }

        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}