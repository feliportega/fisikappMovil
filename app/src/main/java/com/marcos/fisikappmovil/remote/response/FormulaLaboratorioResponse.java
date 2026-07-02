package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class FormulaLaboratorioResponse {

    @SerializedName("id")
    private Integer id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("formula")
    private String formula;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("unidad")
    private String unidad;

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getFormula() {
        return formula;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUnidad() {
        return unidad;
    }

    public String getTituloVisible() {
        if (titulo != null && !titulo.trim().isEmpty()) return titulo;
        if (nombre != null && !nombre.trim().isEmpty()) return nombre;
        return "Fórmula";
    }
}