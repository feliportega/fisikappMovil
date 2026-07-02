package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;

public class MobileAssignmentResponse {

    @SerializedName("assignment_id")
    private int assignmentId;

    @SerializedName("group_id")
    private int groupId;

    @SerializedName("group_name")
    private String groupName;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("due_date")
    private String dueDate;

    @SerializedName("status")
    private String status;

    public int getAssignmentId() {
        return assignmentId;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }
}