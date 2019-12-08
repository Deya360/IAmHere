package com.sse.iamhere;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.PartyAdapter;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.LocaleUtil;
import com.sse.iamhere.Utils.SharedElementTransition.TextDetailBundle;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class MyPartiesActivity extends AppCompatActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EmptySupportedRecyclerView recyclerView;
    private ShimmerFrameLayout placeholderLy;
    private FrameLayout contentLy;

    private PartyAdapter adapter;
    private Parcelable savedRecyclerLayoutState;

    private boolean isDataRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleUtil.setConfigLang(this);
        setContentView(R.layout.activity_my_parties);
        overridePendingTransition(R.anim.push_up_in, R.anim.none);

        if (savedInstanceState!=null){
            savedRecyclerLayoutState = savedInstanceState.getParcelable("mListState");
        }

        setupUI();
        populateAllParties();
    }

    private void setupUI() {
        setTitle(R.string.activity_my_parties_title);

        Toolbar toolbar = findViewById(R.id.my_parties_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        // Setup of swipe to refresh layout
        mSwipeRefreshLayout = findViewById(R.id.my_parties_swipe_refreshLy);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isDataRefreshing) {
                isDataRefreshing = true;
                populateAllParties();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        // Initial setup of recycler view
        recyclerView = findViewById(R.id.my_parties_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PartyAdapter(this::startPartyDetailActivity);
        recyclerView.setAdapter(adapter);

        placeholderLy = findViewById(R.id.my_parties_placeholderLy);
        contentLy = findViewById(R.id.my_parties_contentLy);
    }

    private void populateAllParties() {
        showShimmer();
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {

                @Override
                public void onAttendeePartiesListSuccess(Set<PartyData> partyData) {
                    super.onAttendeePartiesListSuccess(partyData);

                    adapter.setParties(new ArrayList<>(partyData));
                    if (recyclerView.getLayoutManager() != null && savedRecyclerLayoutState != null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);

                    if (partyData.isEmpty()) {
                        recyclerView.setPlaceHolderView(findViewById(R.id.my_parties_empty_view));
                    }
                }

                @Override
                public void onComplete(boolean failed, Integer failCode) {
                    isDataRefreshing = false;
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (failed) {
                        if (failCode == Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), 9000);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }
                    }
                    hideShimmer();
                }
            });
        rb.checkInternet(this).attachToken(this, Constants.TOKEN_ACCESS);
        rb.callRequest(rb::attendeePartiesList);
    }

    private void showShimmer() {
        placeholderLy.startShimmer();
        placeholderLy.setVisibility(View.VISIBLE);
        contentLy.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        placeholderLy.setVisibility(View.GONE);
        contentLy.setVisibility(View.VISIBLE);
    }

    private void showInfoSnackbar(String msg, int duration) {
        if (getWindow().getDecorView().isShown()) {
            if (!TextUtils.isEmpty(msg)) {
                Snackbar.make(findViewById(android.R.id.content), msg, duration).show();
            }
        }
    }


    private void startPartyDetailActivity(int partyId, PartyData partyData, int pos,
                                          View itemLayout, ImageView memberCountIv, TextView memberCountTv, TextView nameTv, TextView descTv) {

        Bundle bundle = new Bundle();
        bundle.putInt("partyId", partyId);
        bundle.putParcelable("partyData", partyData);
        bundle.putInt("itemPos", pos);

        bundle.putParcelable("memberCountTvDetailBundle", new TextDetailBundle(memberCountTv));
        bundle.putParcelable("nameTvDetailBundle", new TextDetailBundle(nameTv));
        bundle.putParcelable("descTvDetailBundle", new TextDetailBundle(descTv));

        Intent intent = new Intent(this, PartyDetailsActivity.class);
        intent.putExtras(bundle);

        @SuppressWarnings("unchecked")
        ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                                                                   Pair.create(itemLayout, getString(R.string.transition_item_layout)),
                                                                   Pair.create(memberCountIv, getString(R.string.transition_member_count_iv)),
                                                                   Pair.create(memberCountTv, getString(R.string.transition_member_count_tv)),
                                                                   Pair.create(nameTv, getString(R.string.transition_name_tv)),
                                                                   Pair.create(descTv, getString(R.string.transition_desc_tv)));

        startActivity(intent, activityOptionsCompat.toBundle());
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
        MyPartiesActivity.this.overridePendingTransition(R.anim.none, R.anim.push_down_out);
    }
}
