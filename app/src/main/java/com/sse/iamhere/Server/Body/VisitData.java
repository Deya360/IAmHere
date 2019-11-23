package com.sse.iamhere.Server.Body;

import java.util.ArrayList;

public class VisitData {
    private int id;
    private String name;
    private ArrayList<Long> visits;

    public VisitData(int id, String name, ArrayList<Long> visits) {
        this.id = id;
        this.name = name;
        this.visits = visits;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Long> getVisits() {
        return visits;
    }

    public void setVisits(ArrayList<Long> visits) {
        this.visits = visits;
    }
}
