package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MobileSimulationResponse {

    @SerializedName("lab_key")
    private String labKey;

    @SerializedName("unity_scene_name")
    private String unitySceneName;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("version")
    private String version;

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("intro_title")
    private String introTitle;

    @SerializedName("intro_text")
    private String introText;

    @SerializedName("instructions")
    private List<String> instructions;

    @SerializedName("max_attempts")
    private int maxAttempts;

    @SerializedName("allow_resume")
    private boolean allowResume;

    @SerializedName("requires_camera")
    private boolean requiresCamera;

    @SerializedName("options")
    private MobileSimulationOptionsResponse options;

    @SerializedName("parameters")
    private MobileSimulationParametersResponse parameters;

    public String getLabKey() {
        return labKey;
    }

    public String getUnitySceneName() {
        return unitySceneName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVersion() {
        return version;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getIntroTitle() {
        return introTitle;
    }

    public String getIntroText() {
        return introText;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public boolean isAllowResume() {
        return allowResume;
    }

    public boolean isRequiresCamera() {
        return requiresCamera;
    }

    public MobileSimulationOptionsResponse getOptions() {
        return options;
    }

    public MobileSimulationParametersResponse getParameters() {
        return parameters;
    }
}