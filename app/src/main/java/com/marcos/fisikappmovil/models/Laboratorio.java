package com.marcos.fisikappmovil.models;

import java.util.List;

public class Laboratorio {

    private int id;
    private String titulo_lab;
    private String resumen;
    private String prologo;
    private String introduccion;
    private String marco_teorico;
    private boolean estado;
    private boolean ra;
    private int categoria;
    private int objetivo;
    private List<Integer> palabras_clave;

    public int getId() {
        return id;
    }

    public String getTitulo_lab() {
        return titulo_lab;
    }

    public String getResumen() {
        return resumen;
    }

    public String getPrologo() {
        return prologo;
    }

    public String getIntroduccion() {
        return introduccion;
    }

    public String getMarco_teorico() {
        return marco_teorico;
    }

    public boolean isEstado() {
        return estado;
    }

    public boolean isRa() {
        return ra;
    }

    public int getCategoria() {
        return categoria;
    }

    public int getObjetivo() {
        return objetivo;
    }

    public List<Integer> getPalabras_clave() {
        return palabras_clave;
    }
}
