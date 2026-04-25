package com.marcos.fisikappmovil.models;

public class Recomendacion {
    private int id;
    private String sugerencia;
    private int informe;

    public Recomendacion(String sugerencia, int informe) {
        this.sugerencia = sugerencia;
        this.informe = informe;
    }

    public int getId() { return id; }
    public String getSugerencia() { return sugerencia; }
    public void setSugerencia(String sugerencia) { this.sugerencia = sugerencia; }
    public int getInforme() { return informe; }
    public void setInforme(int informe) { this.informe = informe; }
}