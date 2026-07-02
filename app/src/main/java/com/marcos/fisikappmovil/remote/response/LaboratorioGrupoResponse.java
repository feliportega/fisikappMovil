package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class LaboratorioGrupoResponse {

    @SerializedName("asignacion_id")
    private int asignacionId;

    @SerializedName("laboratorio_id")
    private int laboratorioId;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("estado_asignacion")
    private String estadoAsignacion;

    @SerializedName("estado_entrega")
    private String estadoEntrega;

    @SerializedName("fecha_inicio")
    private String fechaInicio;

    @SerializedName("fecha_limite")
    private String fechaLimite;

    @SerializedName("fecha_entrega")
    private String fechaEntrega;

    @SerializedName("nota")
    private Double nota;

    @SerializedName("calificacion_estado")
    private String calificacionEstado;

    public int getAsignacionId() { return asignacionId; }
    public int getLaboratorioId() { return laboratorioId; }
    public String getTitulo() { return titulo; }
    public String getCategoria() { return categoria; }
    public String getEstadoAsignacion() { return estadoAsignacion; }
    public String getEstadoEntrega() { return estadoEntrega; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaLimite() { return fechaLimite; }
    public String getFechaEntrega() { return fechaEntrega; }
    public Double getNota() { return nota; }
    public String getCalificacionEstado() { return calificacionEstado; }
}