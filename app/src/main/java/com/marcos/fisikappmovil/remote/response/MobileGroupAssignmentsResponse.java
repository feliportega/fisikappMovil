package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MobileGroupAssignmentsResponse {

    @SerializedName("grupo")
    private MobileGroupResponse grupo;

    @SerializedName("laboratorios")
    private List<MobileAssignmentResponse> laboratorios;

    public MobileGroupResponse getGrupo() {
        return grupo;
    }

    public List<MobileAssignmentResponse> getLaboratorios() {
        return laboratorios;
    }
}