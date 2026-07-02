package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class PracticaLaboratorioResponse {

    @SerializedName("id")
    private Integer id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("materiales")
    private String materiales;

    @SerializedName("instrucciones")
    private String instrucciones;

    @SerializedName("imagen_url")
    private String imagenUrl;

    public Integer getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getMateriales() {
        return materiales;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }
}