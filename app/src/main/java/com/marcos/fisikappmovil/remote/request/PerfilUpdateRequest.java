package com.marcos.fisikappmovil.remote.request;

import com.google.gson.annotations.SerializedName;

public class PerfilUpdateRequest {

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("fecha_nacimiento")
    private String fechaNacimiento;

    @SerializedName("embedding_facial")
    private String embeddingFacial;

    @SerializedName("autorizacion_datos")
    private Boolean autorizacionDatos;

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setEmbeddingFacial(String embeddingFacial) {
        this.embeddingFacial = embeddingFacial;
    }

    public void setAutorizacionDatos(Boolean autorizacionDatos) {
        this.autorizacionDatos = autorizacionDatos;
    }
}