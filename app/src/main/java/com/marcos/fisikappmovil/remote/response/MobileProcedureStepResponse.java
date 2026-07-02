package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class MobileProcedureStepResponse {
    @SerializedName("number")
    private int number;

    @SerializedName("description")
    private String description;

    @SerializedName("image")
    private String image;

    @SerializedName("order")
    private int order;

    public int getNumber() { return number; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public int getOrder() { return order; }
}
