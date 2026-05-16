package com.marcos.fisikappmovil.remote.request;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    private String nombre;
    @SerializedName("correo")
    private String correo;
    private String password;

    public RegisterRequest(String nombre, String correo, String password) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPassword() {
        return password;
    }
}
