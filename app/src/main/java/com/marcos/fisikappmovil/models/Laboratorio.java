package com.marcos.fisikappmovil.models;

import java.util.List;

public class Laboratorio {

    private int id;

    private int creador;

    private String codigo_lab;

    private String titulo_lab;

    private String resumen;

    private String prologo;

    private String introduccion;

    private String marco_teorico;

    private boolean estado;

    private boolean ra;

    private String fecha_creacion;

    private String fecha_actualizacion;

    private int categoria;

    private int objetivo;

    private List<Integer> palabras_clave;

    public Laboratorio(List<Laboratorio> lista) {
    }

    public int getId() {
        return id;
    }

    public String getTitulo_lab() {
        return titulo_lab;
    }

    public String getPrologo() {
        return prologo;
    }
    public void setPrologo(String prologo){this.prologo = prologo;}
    public String getCodigo_lab(){return codigo_lab;}
    public void setCodigo_lab(String codigo_lab){this.codigo_lab = codigo_lab;}

    public String getResumen() {
        return resumen;
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
}
