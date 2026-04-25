package com.marcos.fisikappmovil.models;

public class Informe {
    private String materiales;
    private String observaciones;
    private String conclusiones;

    public Informe(String materiales, String observaciones, String conclusiones) {
        this.materiales = materiales;
        this.observaciones = observaciones;
        this.conclusiones = conclusiones;
    }

    public String getMateriales() { return materiales; }
    public void setMateriales(String materiales) { this.materiales = materiales; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getConclusiones() { return conclusiones; }
    public void setConclusiones(String conclusiones) { this.conclusiones = conclusiones; }
}