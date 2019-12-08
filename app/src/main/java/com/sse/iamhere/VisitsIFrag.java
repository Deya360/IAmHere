package com.sse.iamhere;


import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.VisitIAdapter;
import com.sse.iamhere.Adapters.VisitIViewHolders.VisitIHeaderItem;
import com.sse.iamhere.Adapters.VisitIViewHolders.VisitIItem;
import com.sse.iamhere.Server.Body.CredentialData;
import com.sse.iamhere.Server.Body.PartyBrief;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.Body.VisitIData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PreferencesUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class VisitsIFrag extends Fragment {
    private int eventId = -1;
    private VisitIAdapter adapter;
    private @Nullable SubjectData eventData;
    private long eventDateInMillis;
    private Parcelable savedRecyclerLayoutState;

    private RelativeTimeTextView lastUpdateTv;
    private EmptySupportedRecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_visits_inner, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState!=null){
            eventId = savedInstanceState.getInt("eventId");
            eventDateInMillis = savedInstanceState.getLong("eventDateInMillis");
            eventData = savedInstanceState.getParcelable("eventData");
            savedRecyclerLayoutState = savedInstanceState.getParcelable("mListState");

        } else {
            Bundle extras = getArguments();
            if (extras!=null) {
                eventId = extras.getInt("eventId", eventId);
                eventDateInMillis = extras.getLong("eventDateInMillis");
                eventData = extras.getParcelable("eventData");
            }
        }

        setupUI();
        loadVisits();
    }

    private void setupUI() {
        // Initial setup of recycler view
        recyclerView = getActivity().findViewById(R.id.visitsI_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new VisitIAdapter(new ArrayList<>(), attendeeId -> {
            RequestBuilder rb = new RequestBuilder()
                .setCallback(new RequestsCallback() {
                    @Override
                    public void onGetUser(CredentialData userData) {
                        super.onGetUser(userData);

                        String msg = "User: " + userData.getName();
                        if (!TextUtils.isEmpty(userData.getEmail()) && !userData.getEmail().equals("null")) {
                            msg+= "\n" + "Email: " + userData.getEmail();
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("User Info");
                        builder.setMessage(msg);
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                    }
                });
            rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);

            Constants.Role role = PreferencesUtil.getRole(getActivity(), Constants.Role.NONE);
            switch (role) {
                case ATTENDEE: rb.callRequest(() -> { rb.attendeeGetUserById(attendeeId, "ACCOUNT_PARTICIPATOR"); }); break;
                case HOST: rb.callRequest(() -> { rb.hostGetUserById(attendeeId, "ACCOUNT_PARTICIPATOR"); }); break;
                default:
                    throw new RuntimeException("Invite Code Dialog can only be used for Roles: Attendee, Host");
            }
        });
        recyclerView.setAdapter(adapter);

        // setup of other views
        ProgressBar progressView = getActivity().findViewById(R.id.visitsI_progress_view);
        recyclerView.setEmptyView(progressView);

        ImageView updateIv = getActivity().findViewById(R.id.visitsI_updateIv);
        updateIv.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                recyclerView.setEmptyView(progressView);
                loadVisits();
            }
        });

        lastUpdateTv = getActivity().findViewById(R.id.visitsI_last_updateTv);
    }

    private void loadVisits() {
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onHostGetAttendanceSuccess(Set<PartyBrief> visits) {
                    super.onHostGetAttendanceSuccess(visits);
                    adapter.setParentList(createItems(visits), true);

                    if (getActivity()!=null) {
                        if (recyclerView.getLayoutManager() != null && savedRecyclerLayoutState != null)
                            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                        if (visits.isEmpty()) {
                            recyclerView.setPlaceHolderView(getActivity().findViewById(R.id.visitsI_empty_view));
                        }

                        if (getActivity()!=null)
                            showInfoToast(getString(R.string.msg_updated), Toast.LENGTH_SHORT);
                    }
                }

                @Override
                public void onComplete(boolean failed, Integer failCode) {
                    lastUpdateTv.setReferenceTime(new Date().getTime());

                    if (failed && getActivity()!=null) {
                        if (failCode == Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), 5000);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }
                    }
                }
            });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);
        rb.callRequest(() -> rb.hostGetAttendance(eventId, eventDateInMillis, getEventParties(eventData.getParties())));
    }

    private ArrayList<VisitIHeaderItem> createItems(Set<PartyBrief> visits) {
        ArrayList<VisitIHeaderItem> returnList = new ArrayList<>();
        for (PartyBrief pb : visits) {
            ArrayList<VisitIItem> visitItems = new ArrayList<>();
            for (VisitIData vd : pb.getAttendeesVisits()) {
                visitItems.add(new VisitIItem(vd));
            }

            returnList.add(new VisitIHeaderItem(pb.getName(), visitItems, true));
        }
        return returnList;
    }

    private ArrayList<Integer> getEventParties(ArrayList<PartyBrief> partyBriefs) {
        ArrayList<Integer> returnArr = new ArrayList<>();

        if (partyBriefs==null) return returnArr;
        for (PartyBrief p: partyBriefs) {
            returnArr.add(Integer.valueOf(p.getId()));
        }
        return returnArr;
    }

    private void showInfoSnackbar(String msg, int duration) {
        if (getActivity()!=null) {
            if (getActivity().getWindow().getDecorView().isShown()) {
                if (!TextUtils.isEmpty(msg)) {
                    Snackbar.make(getActivity().findViewById(R.id.event_details_snackbarLy), msg, duration).show();
                }
            }
        }
    }

    private void showInfoToast(String msg, int duration) {
        if (getActivity()!=null) {
            if (getActivity().getWindow().getDecorView().isShown()) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getActivity(), msg, duration).show();
                }
            }
        }
    }

    public void onParentRefresh(SubjectData eventData) {
        this.eventData = eventData;
        loadVisits();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("eventId", eventId);
        outState.putLong("eventDateInMillis", eventDateInMillis);
        outState.putParcelable("eventData", eventData);
    }
}
