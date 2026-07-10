package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class SubmitLaboratorioResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("assignment_id")
    private int assignmentId;

    @SerializedName("entrega_id")
    private int entregaId;

    @SerializedName("entrega_unificada_id")
    private int entregaUnificadaId;

    @SerializedName("estado")
    private String estado;

    @SerializedName("tipo_reporte")
    private String tipoReporte;

    @SerializedName("fecha_entrega")
    private String fechaEntrega;

    @SerializedName("requires_ai_evaluation")
    private boolean requiresAiEvaluation;

    @SerializedName("requires_teacher_review")
    private boolean requiresTeacherReview;

    public String getMessage() {
        return message;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public int getEntregaId() {
        return entregaId;
    }

    public int getEntregaUnificadaId() {
        return entregaUnificadaId;
    }

    public String getEstado() {
        return estado;
    }

    public String getTipoReporte() {
        return tipoReporte;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public boolean isRequiresAiEvaluation() {
        return requiresAiEvaluation;
    }

    public boolean isRequiresTeacherReview() {
        return requiresTeacherReview;
    }
}