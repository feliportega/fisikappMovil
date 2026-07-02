package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MobileResourceResponse {

    @SerializedName("assignment")
    private MobileAssignmentResponse assignment;

    @SerializedName("resource")
    private MobileResourceInfoResponse resource;

    @SerializedName("steps")
    private List<MobileStepResponse> steps;

    public MobileAssignmentResponse getAssignment() {
        return assignment;
    }

    public MobileResourceInfoResponse getResource() {
        return resource;
    }

    public List<MobileStepResponse> getSteps() {
        return steps;
    }
}