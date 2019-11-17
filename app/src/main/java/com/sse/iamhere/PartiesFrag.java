package com.sse.iamhere;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.PartyAdapter;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Utils.Constants;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;


public class PartiesFrag extends Fragment {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EmptySupportedRecyclerView recyclerView;

    private PartyAdapter adapter;
    private Parcelable savedRecyclerLayoutState;

    private boolean isDataRefreshing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity()==null) {
            Log.e("PartiesFrag", "Activity is null onCreate");
            return;
        }

        if (getArguments()!=null) {

            setArguments(null);
        }

        if (savedInstanceState!=null){
            savedRecyclerLayoutState = savedInstanceState.getParcelable("mListState");
        }

        setupUI();
        populateParties();
    }

    private void setupUI() {
        Objects.requireNonNull(getActivity()).setTitle(R.string.fragment_parties_title);

        //Swipe to refresh layout setup
        mSwipeRefreshLayout = getActivity().findViewById(R.id.parties_swipe_refreshLy);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isDataRefreshing) {
                isDataRefreshing = true;
                populateParties();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        // Initial setup of recycler view
        recyclerView = getActivity().findViewById(R.id.parties_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PartyAdapter(subjectId -> {

        });
        recyclerView.setAdapter(adapter);

        ProgressBar progressView = getActivity().findViewById(R.id.parties_progress_view);
        recyclerView.setEmptyView(progressView);
    }

    private void populateParties() {
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onHostPartiesListSuccess(Set<PartyData> partyData) {
                    adapter.setParties(new ArrayList<>(partyData));
                    isDataRefreshing = false;
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (recyclerView.getLayoutManager()!=null && savedRecyclerLayoutState!=null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                    if(partyData.isEmpty()) {
                        recyclerView.setPlaceHolderView(getActivity().findViewById(R.id.parties_empty_view));
                    }
                }

                @Override
                public void onFailure(int errorCode) {
                    if (errorCode== Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                        showInfoSnackbar(getString(R.string.splash_connectionTv_label), Snackbar.LENGTH_LONG);

                    } else {
                        showInfoSnackbar(getString(R.string.msg_server_error), Snackbar.LENGTH_LONG);
                    }
                }
            });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);
        rb.callRequest(rb::hostPartiesList);
    }

    private void showInfoSnackbar(String msg, int duration) {
        if (getActivity()!=null) {
            if (getActivity().getWindow().getDecorView().isShown()) {
                if (!TextUtils.isEmpty(msg)) {
                    Snackbar.make(getActivity().findViewById(R.id.home_mainLy), msg, duration).show();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        RecyclerView.LayoutManager mListState = recyclerView.getLayoutManager();
        if (mListState!=null) {
            outState.putParcelable("mListState", mListState.onSaveInstanceState());
        }
    }
}
