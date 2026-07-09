package com.marcos.fisikappmovil.remote.request;

import com.google.gson.annotations.SerializedName;

public class UnirseGrupoRequest {

    @SerializedName("codigo")
    private String codigo;

    public UnirseGrupoRequest(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}