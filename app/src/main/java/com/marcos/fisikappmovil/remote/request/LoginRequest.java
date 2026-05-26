package com.marcos.fisikappmovil.remote.request;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {



    String correo;
    String password;

    public LoginRequest(String correo, String password) {
        this.correo = correo;
        this.password = password;
    }

   // public LoginRequest(){
     //   return request;
    //}



    public String getCorreo() {
        return correo;
    }

    public String getPassword() {
        return password;
    }
}
