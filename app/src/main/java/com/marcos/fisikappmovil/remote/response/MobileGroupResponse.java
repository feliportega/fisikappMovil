package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class UnirseGrupoResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("grupo_id")
    private Integer grupoId;

    @SerializedName("grupo_nombre")
    private String grupoNombre;

    public String getMessage() {
        return message;
    }

    public Integer getGrupoId() {
        return grupoId;
    }

    public String getGrupoNombre() {
        return grupoNombre;
    }
}