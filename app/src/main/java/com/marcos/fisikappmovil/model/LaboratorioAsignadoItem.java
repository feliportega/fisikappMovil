package com.marcos.fisikappmovil.model;

public class LaboratorioAsignadoItem {

    private final int asignacionId;
    private final int laboratorioId;
    private final int grupoId;

    private final String titulo;
    private final String labKey;
    private final String unitySceneName;

    private final String estadoAsignacion;
    private final String estadoEntrega;

    private final String fechaInicio;
    private final String fechaFin;

    private final int intentosUsados;
    private final int intentosMaximos;

    private final String calificacionEstado;

    public LaboratorioAsignadoItem(
            int asignacionId,
            int laboratorioId,
            int grupoId,
            String titulo,
            String labKey,
            String unitySceneName,
            String estadoAsignacion,
            String estadoEntrega,
            String fechaInicio,
            String fechaFin,
            int intentosUsados,
            int intentosMaximos,
            String calificacionEstado
    ) {
        this.asignacionId = asignacionId;
        this.laboratorioId = laboratorioId;
        this.grupoId = grupoId;
        this.titulo = titulo;
        this.labKey = labKey;
        this.unitySceneName = unitySceneName;
        this.estadoAsignacion = estadoAsignacion;
        this.estadoEntrega = estadoEntrega;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.intentosUsados = intentosUsados;
        this.intentosMaximos = intentosMaximos;
        this.calificacionEstado = calificacionEstado;
    }

    public int getAsignacionId() {
        return asignacionId;
    }

    public int getLaboratorioId() {
        return laboratorioId;
    }

    public int getGrupoId() {
        return grupoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getLabKey() {
        return labKey;
    }

    public String getUnitySceneName() {
        return unitySceneName;
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

    public String getFechaFin() {
        return fechaFin;
    }

    public int getIntentosUsados() {
        return intentosUsados;
    }

    public int getIntentosMaximos() {
        return intentosMaximos;
    }

    public String getCalificacionEstado() {
        return calificacionEstado;
    }

    public boolean estaDisponible() {
        return "ABIERTO".equalsIgnoreCase(estadoAsignacion)
                && intentosUsados < intentosMaximos;
    }
}