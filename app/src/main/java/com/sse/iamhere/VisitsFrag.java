package com.sse.iamhere;


import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.VisitAdapter;
import com.sse.iamhere.Server.Body.PartyBrief;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.Body.VisitData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Utils.Constants;

import java.util.ArrayList;
import java.util.Set;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class VisitsFrag extends Fragment {
    private int eventId = -1;

    private @Nullable SubjectData subjectData;
    private VisitAdapter adapter;
    private Parcelable savedRecyclerLayoutState;

    private EmptySupportedRecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_visits, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState!=null){
            eventId = savedInstanceState.getInt("eventId");
            subjectData = savedInstanceState.getParcelable("subjectData");
            savedRecyclerLayoutState = savedInstanceState.getParcelable("mListState");

        } else {
            Bundle extras = getArguments();
            if (extras!=null) {
                eventId = extras.getInt("eventId", eventId);
                subjectData = extras.getParcelable("subjectData");
            }
        }

        setupUI();
        populateData();
    }

    private void setupUI() {
        // Initial setup of recycler view
        recyclerView = getActivity().findViewById(R.id.visits_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new VisitAdapter(new VisitAdapter.VisitAdapterListener() {
            @Override
            public void onClick(int attendeeId) {
                // on click of item in the visits list
            }
        });
        recyclerView.setAdapter(adapter);

        ProgressBar progressView = getActivity().findViewById(R.id.visits_progress_view);
        recyclerView.setEmptyView(progressView);
    }

    private void populateData() {
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onHostGetAttendanceSuccess(Set<VisitData> visits) {
                    super.onHostGetAttendanceSuccess(visits);
                    adapter.setVisits(new ArrayList<>(visits));

                    if (recyclerView.getLayoutManager() != null && savedRecyclerLayoutState != null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                    if (visits.isEmpty()) {
                        recyclerView.setPlaceHolderView(getActivity().findViewById(R.id.visits_empty_view));
                    }
                }

                @Override
                public void onFailure(int errorCode) {
                    super.onFailure(errorCode);
                    if (errorCode == Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                        showInfoSnackbar(getString(R.string.splash_connectionTv_label), Snackbar.LENGTH_LONG);

                    } else {
                        showInfoSnackbar(getString(R.string.msg_server_error), Snackbar.LENGTH_LONG);
                    }
                }
            });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);
        rb.callRequest(() -> rb.hostGetAttendance(eventId, System.currentTimeMillis(), getEventParties(subjectData.getParties())));
    }

    private ArrayList<Integer> getEventParties(ArrayList<PartyBrief> partyBriefs) {
        ArrayList<Integer> returnArr = new ArrayList<>();

        if (partyBriefs==null) return returnArr;
        for (PartyBrief p: partyBriefs) {
            returnArr.add(Integer.parseInt(p.getId()));
        }
        return returnArr;
    }

    private void showInfoSnackbar(String msg, int duration) {
        if (getActivity()!=null) {
            if (getActivity().getWindow().getDecorView().isShown()) {
                if (!TextUtils.isEmpty(msg)) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), msg, duration).show();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("eventId", eventId);
        outState.putParcelable("subjectData", subjectData);
    }
}
