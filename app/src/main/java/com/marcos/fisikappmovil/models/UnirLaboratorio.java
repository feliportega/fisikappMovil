package com.marcos.fisikappmovil.models;

public class UnirLaboratorio {

    private int id;
    private String codigo_lab;

    public UnirLaboratorio(String codigo_lab) {
        this.codigo_lab = codigo_lab;
    }

    public int getId() {
        return id;
    }

    public String getCodigo_lab() {
        return codigo_lab;
    }

    public void setCodigo_lab(String codigo_lab) {
        this.codigo_lab = codigo_lab;
    }
}