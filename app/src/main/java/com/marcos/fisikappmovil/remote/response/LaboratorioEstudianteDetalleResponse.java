package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LaboratorioEstudianteDetalleResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("titulo_lab")
    private String tituloLab;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("profesor_nombre")
    private String profesorNombre;

    @SerializedName("resumen")
    private String resumen;

    @SerializedName("introduccion")
    private String introduccion;

    @SerializedName("marco_teorico")
    private String marcoTeorico;

    @SerializedName("objetivo_general")
    private String objetivoGeneral;

    @SerializedName("conceptos_basicos")
    private List<ConceptoBasicoResponse> conceptosBasicos;

    @SerializedName("formulas")
    private List<FormulaLaboratorioResponse> formulas;

    @SerializedName("procedimientos")
    private List<ProcedimientoLaboratorioResponse> procedimientos;

    @SerializedName("practicas")
    private List<PracticaLaboratorioResponse> practicas;

    @SerializedName("fecha_creacion")
    private String fechaCreacion;

    public int getId() {
        return id;
    }

    public String getTituloLab() {
        return tituloLab;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getProfesorNombre() {
        return profesorNombre;
    }

    public String getResumen() {
        return resumen;
    }

    public String getIntroduccion() {
        return introduccion;
    }

    public String getMarcoTeorico() {
        return marcoTeorico;
    }

    public String getObjetivoGeneral() {
        return objetivoGeneral;
    }

    public List<ConceptoBasicoResponse> getConceptosBasicos() {
        return conceptosBasicos;
    }

    public List<FormulaLaboratorioResponse> getFormulas() {
        return formulas;
    }

    public List<ProcedimientoLaboratorioResponse> getProcedimientos() {
        return procedimientos;
    }

    public List<PracticaLaboratorioResponse> getPracticas() {
        return practicas;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }
}