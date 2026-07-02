package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class AsignacionDetalleResponse {

    @SerializedName("asignacion_id")
    private int asignacionId;

    @SerializedName("grupo")
    private String grupo;

    @SerializedName("estado_entrega")
    private String estadoEntrega;

    @SerializedName("fecha_entrega")
    private String fechaEntrega;

    @SerializedName("fecha_limite")
    private String fechaLimite;

    @SerializedName("nota")
    private Double nota;

    @SerializedName("laboratorio")
    private LaboratorioDetalleInternoResponse laboratorio;

    public int getAsignacionId() { return asignacionId; }
    public String getGrupo() { return grupo; }
    public String getEstadoEntrega() { return estadoEntrega; }
    public String getFechaEntrega() { return fechaEntrega; }
    public String getFechaLimite() { return fechaLimite; }
    public Double getNota() { return nota; }
    public LaboratorioDetalleInternoResponse getLaboratorio() { return laboratorio; }
}