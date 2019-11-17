package com.sse.iamhere.Server.Body;

import com.google.gson.annotations.SerializedName;

public class SubjectData {
    @SerializedName("id")
    private Integer id;

    @SerializedName("name")
    private String name;

    @SerializedName("plan")
    private Integer plan;

    @SerializedName("description")
    private String description;

    @SerializedName("start_date")
    private long startDate;

    @SerializedName("finish_date")
    private long finishDate;

    public SubjectData(Integer subjectId, String name, Integer plan, String description, long startDate, long finishDate) {
        this.id = subjectId;
        this.name = name;
        this.plan = plan;
        this.description = description;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPlan() {
        return plan;
    }

    public void setPlan(Integer plan) {
        this.plan = plan;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(long finishDate) {
        this.finishDate = finishDate;
    }
}
