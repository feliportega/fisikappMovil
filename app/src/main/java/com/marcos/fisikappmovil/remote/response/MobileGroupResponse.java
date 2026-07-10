package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class MobileGroupResponse {

    @SerializedName("grupo_id")
    private int grupoId;

    @SerializedName("grupo_nombre")
    private String grupoNombre;

    @SerializedName("grado")
    private String grado;

    @SerializedName("jornada")
    private String jornada;

    @SerializedName("codigo_ingreso")
    private String codigoIngreso;

    @SerializedName("instructor_nombre")
    private String instructorNombre;

    @SerializedName("total_laboratorios")
    private int totalLaboratorios;

    @SerializedName("laboratorios_activos")
    private int laboratoriosActivos;

    @SerializedName("entregas_pendientes")
    private int entregasPendientes;

    @SerializedName("entregas_enviadas")
    private int entregasEnviadas;

    @SerializedName("calificaciones_pendientes")
    private int calificacionesPendientes;

    public int getGrupoId() {
        return grupoId;
    }

    public String getGrupoNombre() {
        return grupoNombre;
    }

    public String getGrado() {
        return grado;
    }

    public String getJornada() {
        return jornada;
    }

    public String getCodigoIngreso() {
        return codigoIngreso;
    }

    public String getInstructorNombre() {
        return instructorNombre;
    }

    public int getTotalLaboratorios() {
        return totalLaboratorios;
    }

    public int getLaboratoriosActivos() {
        return laboratoriosActivos;
    }

    public int getEntregasPendientes() {
        return entregasPendientes;
    }

    public int getEntregasEnviadas() {
        return entregasEnviadas;
    }

    public int getCalificacionesPendientes() {
        return calificacionesPendientes;
    }
}