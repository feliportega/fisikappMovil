package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class PerfilUpdateResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private PerfilResponse data;

    public String getMessage() {
        return message;
    }

    public PerfilResponse getData() {
        return data;
    }
}