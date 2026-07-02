package com.marcos.fisikappmovil.model;

public class GrupoEstudianteItem {

    private final int id;
    private final String nombre;
    private final String grado;
    private final String jornada;
    private final String instructorNombre;

    private final int totalLaboratorios;
    private final int laboratoriosActivos;
    private final int entregasPendientes;
    private final int entregasEnviadas;
    private final int calificacionesPendientes;

    public GrupoEstudianteItem(
            int id,
            String nombre,
            String grado,
            String jornada,
            String instructorNombre,
            int totalLaboratorios,
            int laboratoriosActivos,
            int entregasPendientes,
            int entregasEnviadas,
            int calificacionesPendientes
    ) {
        this.id = id;
        this.nombre = nombre;
        this.grado = grado;
        this.jornada = jornada;
        this.instructorNombre = instructorNombre;
        this.totalLaboratorios = totalLaboratorios;
        this.laboratoriosActivos = laboratoriosActivos;
        this.entregasPendientes = entregasPendientes;
        this.entregasEnviadas = entregasEnviadas;
        this.calificacionesPendientes = calificacionesPendientes;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGrado() {
        return grado;
    }

    public String getJornada() {
        return jornada;
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

    public String getResumen() {
        return "Activos: " + laboratoriosActivos
                + " · Pendientes: " + entregasPendientes
                + " · Enviados: " + entregasEnviadas;
    }
}