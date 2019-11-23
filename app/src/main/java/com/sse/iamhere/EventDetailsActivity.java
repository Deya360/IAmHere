package com.sse.iamhere;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Utils.Constants;

import net.glxn.qrgen.android.QRCode;

import java.util.Objects;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class EventDetailsActivity extends AppCompatActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TabLayout tabLayout;

    private boolean isDataRefreshing = false;
    private boolean isTakingAttendance = false;
    private int currentTabIdx = 0;
    private int preventBackCount = 1;
    private int eventId = -1;

    private SparseArray<Fragment.SavedState> savedStateSA = new SparseArray<>();
    private @Nullable SubjectData subjectData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        overridePendingTransition(R.anim.zoom_in, R.anim.none);

        if (savedInstanceState!=null){
            eventId = savedInstanceState.getInt("eventId");
            isTakingAttendance = savedInstanceState.getBoolean("isTakingAttendance", isTakingAttendance);
            subjectData = savedInstanceState.getParcelable("subjectData");

        } else {
            Bundle extras = getIntent().getExtras();
            if (extras!=null) {
                eventId = extras.getInt("eventId", eventId);
                subjectData = extras.getParcelable("subjectData");
            }
        }

        setupUI();
        setupTabFragments(savedInstanceState);
        populateEventDetails();
        testGenerateQRCode();
    }



    private void setupUI() {
        setTitle(R.string.activity_event_details_title);

        Toolbar toolbar = findViewById(R.id.event_details_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        tabLayout = findViewById(R.id.event_details_tabLy);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                swapFragments(tab.getPosition(), getFragmentTag(tab.getPosition()));
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
//
//        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
//                android.R.color.holo_green_dark,
//                android.R.color.holo_orange_dark,
//                android.R.color.holo_blue_dark);
//
//
    }

    private void populateEventDetails() {
        TextView nameTv = findViewById(R.id.event_details_nameTv);
        TextView descTv = findViewById(R.id.event_details_descTv);

        nameTv.setText(subjectData.getName());
        descTv.setText(subjectData.getDescription());

//        RequestBuilder rb = new RequestBuilder()
//            .setCallback(new RequestsCallback() {
//                @Override
//                public void onHostEventsListSuccess(Set<SubjectData> subjectData) {
//                    adapter.setSubjects(new ArrayList<>(subjectData));
//                    isDataRefreshing = false;
//                    mSwipeRefreshLayout.setRefreshing(false);
//                    if (recyclerView.getLayoutManager()!=null && savedRecyclerLayoutState!=null)
//                        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
//                    if(subjectData.isEmpty()) {
//                        recyclerView.setPlaceHolderView(findViewById(R.id.all_events_empty_view));
//                    }
//                }
//
//                @Override
//                public void onFailure(int errorCode) {
//                    if (errorCode== Constants.RQM_EC.NO_INTERNET_CONNECTION) {
//                        showInfoSnackbar(getString(R.string.splash_connectionTv_label), Snackbar.LENGTH_LONG);
//
//                    } else {
//                        showInfoSnackbar(getString(R.string.msg_server_error), Snackbar.LENGTH_LONG);
//                    }
//                }
//            });
//        rb.checkInternet(this).attachToken(this, Constants.TOKEN_ACCESS);
//        rb.callRequest(rb::hostEventsList);
    }

    private void testGenerateQRCode() {
        RequestBuilder rb = new RequestBuilder()
                .setCallback(new RequestsCallback() {
                    @Override
                    public void onQRCodeSuccess(String qrcode) {
                        super.onQRCodeSuccess(qrcode);
                        Bitmap myBitmap = QRCode.from(qrcode)
                                .withSize(350, 350).bitmap();

                        ImageView myImage = findViewById(R.id.event_details_qrCodeIv);
                        myImage.setImageBitmap(myBitmap);
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
        rb.checkInternet(this).attachToken(this, TOKEN_ACCESS);
        rb.callRequest(() -> {rb.hostCreateQRCode(eventId);});




    }


    private void setupTabFragments(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedStateSA = savedInstanceState.getSparseParcelableArray("savedStateSA");
            currentTabIdx = savedInstanceState.getInt("currentTabIdx");
            tabLayout.getTabAt(currentTabIdx).select();

        } else {
            swapFragments(0, getResources().getString(R.string.fragment_event_details_tag));
        }
    }

    private void swapFragments(int tabIdx, String tag) {
        if (getSupportFragmentManager().findFragmentByTag(tag) == null) {
            saveFragmentState(tabIdx);
            createFragment(tabIdx, tag);
        }
    }

    private void createFragment(int tabIdx, String tag) {
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.putInt("eventId", eventId);
        bundle.putParcelable("subjectData", subjectData);
        switch (tabIdx) {
            case 0:
                fragment = new EventDetailsFrag(); break;
            case 1: fragment = new VisitsFrag(); break;
            default: throw new RuntimeException("EventDetailsActivity:createFragment No fragment found for tabIdx: " + tabIdx);
        }

        fragment.setArguments(bundle);
        fragment.setInitialSavedState(savedStateSA.get(tabIdx));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.event_details_contentLy, fragment, tag)
                .commit();
    }

    private void saveFragmentState(int tabIdx) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.event_details_contentLy);
        if (currentFragment != null) {
            savedStateSA.put(currentTabIdx,
                    getSupportFragmentManager().saveFragmentInstanceState(currentFragment)
            );
        }
        currentTabIdx = tabIdx;
    }

    private String getFragmentTag(int tabIdx) {
        switch (tabIdx) {
            case 0: return getString(R.string.fragment_event_details_tag);
            case 1: return getString(R.string.fragment_visits_tag);
            default: throw new RuntimeException("EventDetailsActivity:getFragmentTag No fragment tag found for tabIdx: " + tabIdx);
        }
    }



    private void showInfoSnackbar(String msg, int duration) {
        if (getWindow().getDecorView().isShown()) {
            if (!TextUtils.isEmpty(msg)) {
                Snackbar.make(findViewById(android.R.id.content), msg, duration).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isTakingAttendance && preventBackCount > 0) {
            showInfoSnackbar("Attendence is in progress, if you return, it will be stopped", Snackbar.LENGTH_LONG); //todo: temp
            preventBackCount--;
        }

        super.onBackPressed();
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

        outState.putInt("eventId", eventId);
        outState.putBoolean("isTakingAttendance", isTakingAttendance);
        outState.putInt("currentTabIdx", currentTabIdx);
        outState.putSparseParcelableArray("savedStateSA", savedStateSA);
        outState.putParcelable("subjectData", subjectData);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.none, R.anim.zoom_out);
    }
}
