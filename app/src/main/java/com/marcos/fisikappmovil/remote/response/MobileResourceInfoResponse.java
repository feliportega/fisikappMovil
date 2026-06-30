package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class MobileResourceInfoResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private String type;

    @SerializedName("title")
    private String title;

    @SerializedName("category")
    private String category;

    @SerializedName("teacher")
    private String teacher;

    @SerializedName("summary")
    private String summary;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("generated_ai")
    private boolean generatedAi;

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getSummary() {
        return summary;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isGeneratedAi() {
        return generatedAi;
    }
}