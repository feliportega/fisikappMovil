package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class ProcedimientoLaboratorioResponse {

    @SerializedName("id")
    private Integer id;

    @SerializedName("orden")
    private Integer orden;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("instruccion")
    private String instruccion;

    @SerializedName("imagen_url")
    private String imagenUrl;

    public Integer getId() {
        return id;
    }

    public Integer getOrden() {
        return orden;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getInstruccion() {
        return instruccion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public String getTextoVisible() {
        if (instruccion != null && !instruccion.trim().isEmpty()) return instruccion;
        if (descripcion != null && !descripcion.trim().isEmpty()) return descripcion;
        return "";
    }

    public String getTituloVisible() {
        if (titulo != null && !titulo.trim().isEmpty()) return titulo;
        if (orden != null) return "Paso " + orden;
        return "Procedimiento";
    }
}