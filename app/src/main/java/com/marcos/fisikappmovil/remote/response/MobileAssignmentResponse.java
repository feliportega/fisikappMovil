package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class MobileAssignmentResponse {

    @SerializedName("asignacion_id")
    private int asignacionId;

    @SerializedName("laboratorio_id")
    private int laboratorioId;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("instructor_nombre")
    private String instructorNombre;

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

    @SerializedName("tiene_ar")
    private boolean tieneAr;

    @SerializedName("lab_key")
    private String labKey;

    @SerializedName("nota_ia")
    private Double notaIa;

    @SerializedName("nota_docente")
    private Double notaDocente;

    @SerializedName("calificacion_estado")
    private String calificacionEstado;

    @SerializedName("resource_endpoint")
    private String resourceEndpoint;

    public int getAsignacionId() {
        return asignacionId;
    }

    public int getLaboratorioId() {
        return laboratorioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getInstructorNombre() {
        return instructorNombre;
    }

    public String getEstadoAsignacion() {
        return estadoAsignacion;
    }

    public String getEstadoEntrega() {
        return estadoEntrega;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public String getFechaLimite() {
        return fechaLimite;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public boolean isTieneAr() {
        return tieneAr;
    }

    public String getLabKey() {
        return labKey;
    }

    public Double getNotaIa() {
        return notaIa;
    }

    public Double getNotaDocente() {
        return notaDocente;
    }

    public String getCalificacionEstado() {
        return calificacionEstado;
    }

    public String getResourceEndpoint() {
        return resourceEndpoint;
    }
}