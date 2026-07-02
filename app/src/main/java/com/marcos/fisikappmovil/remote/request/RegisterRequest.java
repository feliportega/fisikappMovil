package com.marcos.fisikappmovil.remote.request;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    private String nombre;
    private String identificacion;
    
    @SerializedName("fecha_nacimiento")
    private String fechaNacimiento;
    
    private String institucion;
    
    @SerializedName("correo")
    private String correo;
    
    private String password;

    public RegisterRequest(String nombre, String identificacion, String fechaNacimiento, String institucion, String correo, String password) {
        this.nombre = nombre;
        this.identificacion = identificacion;
        this.fechaNacimiento = fechaNacimiento;
        this.institucion = institucion;
        this.correo = correo;
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getInstitucion() {
        return institucion;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPassword() {
        return password;
    }
}
