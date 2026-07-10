package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.ar;

public class ArStepConfig {

    private final int arId;
    private final String labKey;
    private final String unitySceneName;
    private final String displayName;

    public ArStepConfig(
            int arId,
            String labKey,
            String unitySceneName,
            String displayName
    ) {
        this.arId = arId;
        this.labKey = labKey;
        this.unitySceneName = unitySceneName;
        this.displayName = displayName;
    }

    public int getArId() {
        return arId;
    }

    public String getLabKey() {
        return labKey;
    }

    public String getUnitySceneName() {
        return unitySceneName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasValidArId() {
        return arId > 0;
    }
}