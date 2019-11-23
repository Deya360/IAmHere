package com.sse.iamhere;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.EventAdapter;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Utils.Constants;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class AllEventsActivity extends AppCompatActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EmptySupportedRecyclerView recyclerView;

    private EventAdapter adapter;
    private Parcelable savedRecyclerLayoutState;

    private boolean isDataRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_events);
        overridePendingTransition(R.anim.push_up_in, R.anim.none);

        if (savedInstanceState!=null){
            savedRecyclerLayoutState = savedInstanceState.getParcelable("mListState");
        }

        setupUI();
        populateAllEvents();
    }

    private void setupUI() {
        setTitle(R.string.activity_all_events_title);

        Toolbar toolbar = findViewById(R.id.all_events_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        // Setup of swipe to refresh layout
        mSwipeRefreshLayout = findViewById(R.id.all_events_swipe_refreshLy);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isDataRefreshing) {
                isDataRefreshing = true;
                populateAllEvents();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        // Initial setup of recycler view
        recyclerView = findViewById(R.id.all_events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventAdapter(this::startEventDetailActivity);
        recyclerView.setAdapter(adapter);

        ProgressBar progressView = findViewById(R.id.all_events_progress_view);
        recyclerView.setEmptyView(progressView);
    }

    private void populateAllEvents() {
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onHostEventsListSuccess(Set<SubjectData> subjectData) {
                    super.onHostEventsListSuccess(subjectData);
                    adapter.setSubjects(new ArrayList<>(subjectData));
                    isDataRefreshing = false;
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (recyclerView.getLayoutManager() != null && savedRecyclerLayoutState != null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                    if (subjectData.isEmpty()) {
                        recyclerView.setPlaceHolderView(findViewById(R.id.all_events_empty_view));
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
        rb.checkInternet(this).attachToken(this, Constants.TOKEN_ACCESS);
        rb.callRequest(rb::hostEventsList);
    }

    private void showInfoSnackbar(String msg, int duration) {
        if (getWindow().getDecorView().isShown()) {
            if (!TextUtils.isEmpty(msg)) {
                Snackbar.make(findViewById(android.R.id.content), msg, duration).show();
            }
        }
    }

    private void startEventDetailActivity(SubjectData subjectData, int eventId) {
        Bundle bundle = new Bundle();
        bundle.putInt("eventId", eventId);
        bundle.putParcelable("subjectData", subjectData);
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        RecyclerView.LayoutManager mListState = recyclerView.getLayoutManager();
        if (mListState!=null) {
            outState.putParcelable("mListState", mListState.onSaveInstanceState());
        }
    }

    @Override
    public void finish() {
        super.finish();
        AllEventsActivity.this.overridePendingTransition(R.anim.none, R.anim.push_down_out);
    }
}
