package com.marcos.fisikappmovil.models;

public class UnirLaboratorio {

    private int id;
    private String fecha_inscripcion;
    private int usuario;
    private int laboratorio;


    private String codigo_lab;

    public UnirLaboratorio(String codigo_lab) {
        this.codigo_lab = codigo_lab;
    }

    public int getId() {
        return id;
    }

    public String getFecha_inscripcion() {
        return fecha_inscripcion;
    }

    public int getUsuario() {
        return usuario;
    }

    public int getLaboratorio() {
        return laboratorio;
    }

    public String getCodigo_lab() {
        return codigo_lab;
    }
}