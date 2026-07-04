package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class MobileSimulationParametersResponse {

    @SerializedName("gravity")
    private double gravity;

    @SerializedName("gravity_unit")
    private String gravityUnit;

    @SerializedName("min_velocity")
    private double minVelocity;

    @SerializedName("max_velocity")
    private double maxVelocity;

    @SerializedName("velocity_unit")
    private String velocityUnit;

    @SerializedName("min_angle")
    private double minAngle;

    @SerializedName("max_angle")
    private double maxAngle;

    @SerializedName("angle_unit")
    private String angleUnit;

    public double getGravity() {
        return gravity;
    }

    public String getGravityUnit() {
        return gravityUnit;
    }

    public double getMinVelocity() {
        return minVelocity;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }

    public String getVelocityUnit() {
        return velocityUnit;
    }

    public double getMinAngle() {
        return minAngle;
    }

    public double getMaxAngle() {
        return maxAngle;
    }

    public String getAngleUnit() {
        return angleUnit;
    }
}