package com.marcos.fisikappmovil.model;

public class GrupoAcademicoItem {

    private final int id;
    private final String nombre;
    private final String institucion;
    private final String profesor;
    private final int totalActividades;
    private final int pendientes;
    private final int entregadas;
    private final int calificadas;
    private final boolean activo;

    public GrupoAcademicoItem(
            int id,
            String nombre,
            String institucion,
            String profesor,
            int totalActividades,
            int pendientes,
            int entregadas,
            int calificadas,
            boolean activo
    ) {
        this.id = id;
        this.nombre = nombre;
        this.institucion = institucion;
        this.profesor = profesor;
        this.totalActividades = totalActividades;
        this.pendientes = pendientes;
        this.entregadas = entregadas;
        this.calificadas = calificadas;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getInstitucion() {
        return institucion;
    }

    public String getProfesor() {
        return profesor;
    }

    public int getTotalActividades() {
        return totalActividades;
    }

    public int getPendientes() {
        return pendientes;
    }

    public int getEntregadas() {
        return entregadas;
    }

    public int getCalificadas() {
        return calificadas;
    }

    public boolean isActivo() {
        return activo;
    }
}
