package com.marcos.fisikappmovil.models;

public class Incripcion {
    private int id;
    private String fecha_inscripcion;
    private int usuario;
    private int laboratorio; // Este es el ID del laboratorio

    public int getId() { return id; }
    public String getFecha_inscripcion() { return fecha_inscripcion; }
    public int getUsuario() { return usuario; }
    public int getLaboratorio() { return laboratorio; }
}
