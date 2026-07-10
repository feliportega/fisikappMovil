package com.marcos.fisikappmovil.remote.request;

import com.google.gson.annotations.SerializedName;

public class JoinGroupRequest {

    @SerializedName("codigo")
    private final String codigo;

    public JoinGroupRequest(String codigo) {
        this.codigo = codigo;
    }
}