package com.marcos.fisikappmovil.remote.request;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    @SerializedName("correo")
    private String correo;
    private String uid;
    private String token;
    private String password;

    public ResetPasswordRequest(String correo, String uid, String token, String password) {
        this.correo = correo;
        this.uid = uid;
        this.token = token;
        this.password = password;
    }

    public String getCorreo() {
        return correo;
    }

    public String getUid() {
        return uid;
    }

    public String getToken() {
        return token;
    }

    public String getPassword() {
        return password;
    }
}
