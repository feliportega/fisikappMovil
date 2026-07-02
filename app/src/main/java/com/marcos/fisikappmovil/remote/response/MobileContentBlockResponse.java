package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class MobileContentBlockResponse {

    @SerializedName("type")
    private String type;

    @SerializedName("value")
    private String value;

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}