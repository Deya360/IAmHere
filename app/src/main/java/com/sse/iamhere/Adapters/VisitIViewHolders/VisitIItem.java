package com.sse.iamhere.Adapters.VisitIViewHolders;

import com.sse.iamhere.Server.Body.VisitIData;

public class VisitIItem {
    private VisitIData visit;

    public VisitIItem(VisitIData visit) {
        this.visit = visit;
    }

    public VisitIData getVisit() {
        return visit;
    }
}
