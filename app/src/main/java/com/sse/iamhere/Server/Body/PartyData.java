package com.sse.iamhere.Server.Body;

import com.google.gson.annotations.SerializedName;

public class PartyData {
    @SerializedName("party_id")
    private final String partyId;

    @SerializedName("party_name")
    private final String partyName;

    @SerializedName("manager_name")
    private final String managerName;

    @SerializedName("description")
    private final String description;

    @SerializedName("participators_count")
    private final String attendeeCount;

    public PartyData(String partyId, String partyName, String managerName, String description, String attendeeCount) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.managerName = managerName;
        this.description = description;
        this.attendeeCount = attendeeCount;
    }

    public String getPartyId() {
        return partyId;
    }

    public String getPartyName() {
        return partyName;
    }

    public String getManagerName() {
        return managerName;
    }

    public String getDescription() {
        return description;
    }

    public String getAttendeeCount() {
        return attendeeCount;
    }
}
