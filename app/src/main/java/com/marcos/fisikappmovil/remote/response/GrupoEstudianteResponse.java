package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GrupoEstudianteResponse {

    @SerializedName("grupo_id")
    private int grupoId;

    @SerializedName("grupo_nombre")
    private String grupoNombre;

    @SerializedName("grado")
    private String grado;

    @SerializedName("jornada")
    private String jornada;

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

    @SerializedName("actividades")
    private List<ActividadGrupoResponse> actividades;

    public int getGrupoId() { return grupoId; }
    public String getGrupoNombre() { return grupoNombre; }
    public String getGrado() { return grado; }
    public String getJornada() { return jornada; }
    public String getInstructorNombre() { return instructorNombre; }
    public int getTotalLaboratorios() { return totalLaboratorios; }
    public int getLaboratoriosActivos() { return laboratoriosActivos; }
    public int getEntregasPendientes() { return entregasPendientes; }
    public int getEntregasEnviadas() { return entregasEnviadas; }
    public int getCalificacionesPendientes() { return calificacionesPendientes; }
    public List<ActividadGrupoResponse> getActividades() { return actividades; }
}