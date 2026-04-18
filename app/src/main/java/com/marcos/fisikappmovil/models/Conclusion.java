package com.marcos.fisikappmovil.models;

public class Conclusion {
    private int id;
    private String texto;
    private int informe;

    public Conclusion(String texto, int informe) {
        this.texto = texto;
        this.informe = informe;
    }

    public int getId() { return id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public int getInforme() { return informe; }
    public void setInforme(int informe) { this.informe = informe; }
}