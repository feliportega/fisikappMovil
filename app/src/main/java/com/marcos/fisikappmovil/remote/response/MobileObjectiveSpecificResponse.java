package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class MobileObjectiveSpecificResponse {

    @SerializedName("description")
    private String description;

    public String getDescription() {
        return description;
    }
}