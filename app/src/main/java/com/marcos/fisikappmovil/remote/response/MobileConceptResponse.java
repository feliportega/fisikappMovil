package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MobileConceptResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("example")
    private String example;

    @SerializedName("type")
    private String type;

    @SerializedName("resources")
    private List<MobileResourceLinkResponse> resources;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getExample() {
        return example;
    }

    public String getType() {
        return type;
    }

    public List<MobileResourceLinkResponse> getResources() {
        return resources;
    }
}