package com.sse.iamhere.Adapters.VisitIViewHolders;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.List;

public class VisitIHeaderItem implements Parent<VisitIItem> {
    private String partyName;
    private List<VisitIItem> visits;
    private boolean isExpanded;

    public VisitIHeaderItem(String partyName, List<VisitIItem> visits, boolean isExpanded) {
        this.partyName = partyName;
        this.visits = visits;
        this.isExpanded = isExpanded;
    }

    @Override
    public List<VisitIItem> getChildList() {
        return this.visits;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return isExpanded;
    }

    public String getPartyName() {
        return partyName;
    }
}
