package com.sse.iamhere.Server.Body;

public class PartyData {
    private final String party_id;
    private final String party_name;
    private final String manager_name;
    private final String description;
    private final String participators_count;

    public PartyData(String party_id, String party_name, String manager_name, String description, String participators_count) {
        this.party_id = party_id;
        this.party_name = party_name;
        this.manager_name = manager_name;
        this.description = description;
        this.participators_count = participators_count;
    }
}
