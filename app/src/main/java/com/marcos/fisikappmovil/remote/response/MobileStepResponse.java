package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import com.google.gson.JsonObject;

public class MobileStepResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("order")
    private int order;

    @SerializedName("type")
    private String type;

    @SerializedName("title")
    private String title;

    @SerializedName("required")
    private boolean required;

    @SerializedName("content")
    private List<MobileContentBlockResponse> content;

    @SerializedName("general")
    private MobileObjectiveGeneralResponse general;

    @SerializedName("specifics")
    private List<MobileObjectiveSpecificResponse> specifics;

    @SerializedName("concepts")
    private List<MobileConceptResponse> concepts;

    @SerializedName("formulas")
    private List<MobileFormulaResponse> formulas;

    @SerializedName("steps")
    private List<MobileProcedureStepResponse> procedureSteps;

    @SerializedName("exercise")
    private JsonObject exercise;

    @SerializedName("comparison")
    private JsonObject comparison;

    @SerializedName("report")
    private JsonObject report;

    @SerializedName("submission")
    private JsonObject submission;

    @SerializedName("simulation_ref")
    private JsonObject simulationRef;

    public String getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public boolean isRequired() {
        return required;
    }

    public JsonObject getExercise() {
        return exercise;
    }

    public JsonObject getComparison() {
        return comparison;
    }

    public JsonObject getReport() {
        return report;
    }

    public JsonObject getSubmission() {
        return submission;
    }

    public JsonObject getSimulationRef() {
        return simulationRef;
    }


    public List<MobileContentBlockResponse> getContent() {
        return content;
    }

    public MobileObjectiveGeneralResponse getGeneral() {
        return general;
    }

    public List<MobileObjectiveSpecificResponse> getSpecifics() {
        return specifics;
    }

    public List<MobileConceptResponse> getConcepts() {
        return concepts;
    }

    public List<MobileFormulaResponse> getFormulas() {
        return formulas;
    }

    public List<MobileProcedureStepResponse> getProcedureSteps() {
        return procedureSteps;
    }
}