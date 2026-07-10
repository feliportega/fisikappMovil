package com.marcos.fisikappmovil.remote.request;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("nombre")
    private final String nombre;

    @SerializedName("correo")
    private final String correo;

    @SerializedName("password")
    private final String password;

    @SerializedName("identificacion")
    private final String identificacion;

    @SerializedName("institucion")
    private final String institucion;

    public RegisterRequest(
            String nombre,
            String correo,
            String password,
            String identificacion,
            String institucion
    ) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
        this.identificacion = identificacion;
        this.institucion = institucion;
    }
}