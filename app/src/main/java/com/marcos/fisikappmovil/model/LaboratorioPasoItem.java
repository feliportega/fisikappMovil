package com.marcos.fisikappmovil.model;

public class LaboratorioPasoItem {

    public static final String TIPO_LECTURA = "LECTURA";
    public static final String TIPO_PREGUNTAS = "PREGUNTAS";
    public static final String TIPO_PRACTICA_EXPERIMENTAL = "PRACTICA_EXPERIMENTAL";
    public static final String TIPO_SIMULACION_AR = "SIMULACION_AR";
    public static final String TIPO_DATOS_SIMULACION = "DATOS_SIMULACION";
    public static final String TIPO_DATOS_EXPERIMENTALES = "DATOS_EXPERIMENTALES";
    public static final String TIPO_COMPARACION = "COMPARACION";
    public static final String TIPO_INFORME = "INFORME";
    public static final String TIPO_ENVIO = "ENVIO";

    public static final String ESTADO_BLOQUEADO = "BLOQUEADO";
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_EN_PROGRESO = "EN_PROGRESO";
    public static final String ESTADO_COMPLETADO = "COMPLETADO";

    private final int orden;
    private final String titulo;
    private final String descripcion;
    private final String tipo;
    private final boolean obligatorio;
    private String estado;

    public LaboratorioPasoItem(
            int orden,
            String titulo,
            String descripcion,
            String tipo,
            boolean obligatorio,
            String estado
    ) {
        this.orden = orden;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.obligatorio = obligatorio;
        this.estado = estado;
    }

    public int getOrden() {
        return orden;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean isObligatorio() {
        return obligatorio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean estaBloqueado() {
        return ESTADO_BLOQUEADO.equalsIgnoreCase(estado);
    }

    public boolean estaCompletado() {
        return ESTADO_COMPLETADO.equalsIgnoreCase(estado);
    }
}