package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class ActividadGrupoResponse {

    @SerializedName("laboratorio_id")
    private int laboratorioId;

    @SerializedName("laboratorio")
    private String laboratorio;

    @SerializedName("estado_entrega")
    private String estadoEntrega;

    @SerializedName("fecha_limite")
    private String fechaLimite;

    public int getLaboratorioId() { return laboratorioId; }
    public String getLaboratorio() { return laboratorio; }
    public String getEstadoEntrega() { return estadoEntrega; }
    public String getFechaLimite() { return fechaLimite; }
}