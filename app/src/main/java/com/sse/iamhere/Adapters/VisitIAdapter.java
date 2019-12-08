package com.sse.iamhere.Adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.sse.iamhere.Adapters.VisitIViewHolders.VisitIHeaderItem;
import com.sse.iamhere.Adapters.VisitIViewHolders.VisitIHeaderViewHolder;
import com.sse.iamhere.Adapters.VisitIViewHolders.VisitIItem;
import com.sse.iamhere.Adapters.VisitIViewHolders.VisitIViewHolder;
import com.sse.iamhere.R;

import java.util.ArrayList;
import java.util.List;

public class VisitIAdapter extends ExpandableRecyclerAdapter<VisitIHeaderItem, VisitIItem, VisitIHeaderViewHolder, VisitIViewHolder>
    implements ExpandableRecyclerAdapter.ExpandCollapseListener {
    private static final int VISIT_HEADER = 0;
    private static final int VISIT_ITEM = 1;

    private VisitIViewHolder.VisitAdapterListener visitAdapterListener;
    private ArrayList<Integer> expandedStateMap = new ArrayList<>();

    public VisitIAdapter(@NonNull List<VisitIHeaderItem> items, VisitIViewHolder.VisitAdapterListener ral) {
        super(items);
        this.visitAdapterListener = ral;
    }

    @Override
    public void onParentExpanded(int parentPosition) {
        expandedStateMap.set(parentPosition, 1);
    }

    @Override
    public void onParentCollapsed(int parentPosition) {
        expandedStateMap.set(parentPosition,0);
    }

    @Override
    public void setParentList(@NonNull List<VisitIHeaderItem> parentList, boolean preserveExpansionState) {
        super.setParentList(parentList, preserveExpansionState);
        setHeaderStates(expandedStateMap);
    }

    private void setHeaderStates(ArrayList<Integer> states) {
        List<VisitIHeaderItem> items = getParentList();

        for (int headerIdx = 0; headerIdx < states.size(); headerIdx++) {
            if (states.get(headerIdx).equals(0)) {
                collapseParent(headerIdx);
            } else {
                expandParent(headerIdx);
            }
        }
        super.setParentList(items, true);
    }


    @Override
    public int getParentViewType(int parentPosition) {
        return VISIT_HEADER;
    }

    @Override
    public int getChildViewType(int parentPosition, int childPosition) {
        return VISIT_ITEM;
    }



    @NonNull
    @Override
    public VisitIHeaderViewHolder onCreateParentViewHolder(@NonNull ViewGroup parent, int viewType) {
        View pendingView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visit_inner_header, parent, false);
        return new VisitIHeaderViewHolder(pendingView);
    }

    @NonNull
    @Override
    public VisitIViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visit_inner, parent, false);
        return new VisitIViewHolder(view, visitAdapterListener);
    }

    @Override
    public void onBindParentViewHolder(@NonNull VisitIHeaderViewHolder parentViewHolder, int parentPosition,
                                       @NonNull VisitIHeaderItem parent) {
        parentViewHolder.onBind(parent);

    }

    @Override
    public void onBindChildViewHolder(@NonNull VisitIViewHolder childViewHolder, int parentPosition,
                                      int childPosition, @NonNull VisitIItem child) {
        childViewHolder.onBind(child);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putIntegerArrayList("expandedStateMap", expandedStateMap);
    }

    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            expandedStateMap = savedInstanceState.getIntegerArrayList("expandedStateMap");
        }
    }
}
