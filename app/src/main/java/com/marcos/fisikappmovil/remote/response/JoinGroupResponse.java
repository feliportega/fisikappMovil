package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class JoinGroupResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("created")
    private boolean created;

    @SerializedName("grupo")
    private MobileGroupResponse grupo;

    public String getMessage() {
        return message;
    }

    public boolean isCreated() {
        return created;
    }

    public MobileGroupResponse getGrupo() {
        return grupo;
    }
}