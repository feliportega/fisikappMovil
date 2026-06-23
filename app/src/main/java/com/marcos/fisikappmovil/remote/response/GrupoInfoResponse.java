package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class GrupoInfoResponse {

    @SerializedName("grupo_id")
    private int grupoId;

    @SerializedName("grupo_nombre")
    private String grupoNombre;

    @SerializedName("instructor_nombre")
    private String instructorNombre;

    @SerializedName("grado")
    private String grado;

    @SerializedName("jornada")
    private String jornada;

    public int getGrupoId() { return grupoId; }
    public String getGrupoNombre() { return grupoNombre; }
    public String getInstructorNombre() { return instructorNombre; }
    public String getGrado() { return grado; }
    public String getJornada() { return jornada; }
}