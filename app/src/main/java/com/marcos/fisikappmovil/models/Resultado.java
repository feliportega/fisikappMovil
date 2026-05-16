package com.marcos.fisikappmovil.models;

public class Resultado {
    private int id;
    private String descripcion;
    private int informe;

    public Resultado(String descripcion, int informe) {
        this.descripcion = descripcion;
        this.informe = informe;
    }

    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getInforme() { return informe; }
    public void setInforme(int informe) { this.informe = informe; }

    public class UnirLaboratorio {
        private int id;
        private String codigo_lab;
        }
}