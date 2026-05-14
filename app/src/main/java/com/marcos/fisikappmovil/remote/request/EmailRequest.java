package com.marcos.fisikappmovil.remote.request;

import com.google.gson.annotations.SerializedName;

public class EmailRequest {
    @SerializedName("correo")
    private String correo;

    public EmailRequest(String correo) {
        this.correo = correo;
    }

    public String getCorreo() {
        return correo;
    }
}
