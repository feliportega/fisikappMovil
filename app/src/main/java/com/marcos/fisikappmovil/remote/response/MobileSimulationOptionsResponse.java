package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class MobileSimulationOptionsResponse {

    @SerializedName("language")
    private String language;

    @SerializedName("show_projectile_camera_option")
    private boolean showProjectileCameraOption;

    @SerializedName("show_trajectory_preview")
    private boolean showTrajectoryPreview;

    @SerializedName("show_distance_indicators")
    private boolean showDistanceIndicators;

    @SerializedName("allow_audio")
    private boolean allowAudio;

    public String getLanguage() {
        return language;
    }

    public boolean isShowProjectileCameraOption() {
        return showProjectileCameraOption;
    }

    public boolean isShowTrajectoryPreview() {
        return showTrajectoryPreview;
    }

    public boolean isShowDistanceIndicators() {
        return showDistanceIndicators;
    }

    public boolean isAllowAudio() {
        return allowAudio;
    }
}