package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class LaboratorioDetalleInternoResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("resumen")
    private String resumen;

    @SerializedName("introduccion")
    private String introduccion;

    @SerializedName("marco_teorico")
    private String marcoTeorico;

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getCategoria() { return categoria; }
    public String getResumen() { return resumen; }
    public String getIntroduccion() { return introduccion; }
    public String getMarcoTeorico() { return marcoTeorico; }
}