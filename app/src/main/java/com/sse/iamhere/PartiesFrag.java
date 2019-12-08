package com.sse.iamhere;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
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
import com.sse.iamhere.Utils.SharedElementTransition.TextDetailBundle;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;


public class PartiesFrag extends Fragment {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EmptySupportedRecyclerView recyclerView;
    private ShimmerFrameLayout placeholderLy;
    private FrameLayout contentLy;

    private PartyAdapter adapter;
    private Parcelable savedRecyclerLayoutState;

    private boolean isDataRefreshing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parties, container, false);
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

        adapter = new PartyAdapter(this::startPartyDetailActivity);
        recyclerView.setAdapter(adapter);

        placeholderLy = getActivity().findViewById(R.id.parties_placeholderLy);
        contentLy = getActivity().findViewById(R.id.parties_contentLy);
    }

    private void populateParties() {
        showShimmer();
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onHostPartiesListSuccess(Set<PartyData> partyData) {
                    super.onHostPartiesListSuccess(partyData);
                    adapter.setParties(new ArrayList<>(partyData));
                    if (recyclerView.getLayoutManager() != null && savedRecyclerLayoutState != null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                    if (partyData.isEmpty()) {
                        recyclerView.setPlaceHolderView(getActivity().findViewById(R.id.parties_empty_view));
                    }
                }

                @Override
                public void onComplete(boolean failed, Integer failCode) {
                    isDataRefreshing = false;
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (failed  && getActivity()!=null) {
                        if (failCode== Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), 5000);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }
                    }
                    hideShimmer();
                }
            });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);
        rb.callRequest(rb::hostPartiesList);
    }

    private void startPartyDetailActivity(int partyId, PartyData partyData, int pos,
                      View itemLayout, ImageView memberCountIv, TextView memberCountTv, TextView nameTv, TextView descTv) {

        if (getActivity()==null) {
            Log.e("PartiesFrag", "getActivity is null startPartyDetailActivity");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putInt("partyId", partyId);
        bundle.putParcelable("partyData", partyData);
        bundle.putInt("itemPos", pos);

        bundle.putParcelable("memberCountTvDetailBundle", new TextDetailBundle(memberCountTv));
        bundle.putParcelable("nameTvDetailBundle", new TextDetailBundle(nameTv));
        bundle.putParcelable("descTvDetailBundle", new TextDetailBundle(descTv));

        Intent intent = new Intent(getActivity(), PartyDetailsActivity.class);
        intent.putExtras(bundle);

        @SuppressWarnings("unchecked")
        ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                           Pair.create(itemLayout, getString(R.string.transition_item_layout)),
                           Pair.create(memberCountIv, getString(R.string.transition_member_count_iv)),
                           Pair.create(memberCountTv, getString(R.string.transition_member_count_tv)),
                           Pair.create(nameTv, getString(R.string.transition_name_tv)),
                           Pair.create(descTv, getString(R.string.transition_desc_tv)));

        startActivity(intent, activityOptionsCompat.toBundle());
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

    private void showShimmer() {
        placeholderLy.startShimmer();
        placeholderLy.setVisibility(View.VISIBLE);
        contentLy.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        placeholderLy.setVisibility(View.GONE);
        contentLy.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        RecyclerView.LayoutManager mListState = recyclerView.getLayoutManager();
        if (mListState!=null) {
            outState.putParcelable("mListState", mListState.onSaveInstanceState());
        }
    }

    public void onActivityReenter(int resultCode, Intent data) {
        //todo
//        setExitSharedElementCallback(new SharedElementEnterCallback());
//        recyclerView.getLayoutManager().findViewByPosition(2).get
    }
}
