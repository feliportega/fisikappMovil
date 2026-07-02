package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class ConceptoBasicoResponse {

    @SerializedName("id")
    private Integer id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("contenido")
    private String contenido;

    public Integer getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getContenido() {
        return contenido;
    }

    public String getTextoVisible() {
        if (contenido != null && !contenido.trim().isEmpty()) return contenido;
        if (descripcion != null && !descripcion.trim().isEmpty()) return descripcion;
        return "";
    }

    public String getTituloVisible() {
        if (titulo != null && !titulo.trim().isEmpty()) return titulo;
        if (nombre != null && !nombre.trim().isEmpty()) return nombre;
        return "Concepto";
    }
}