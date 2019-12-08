package com.sse.iamhere;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.LocaleUtil;
import com.sse.iamhere.Utils.TextFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class AttendeeEventDetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AppBarLayout appBarLy;
    private TabLayout tabLayout;

    private boolean isDataRefreshing = false;
    private int currentTabIdx = 0;
    private int preventBackCount = 1;
    private int eventId = -1;
    private long eventDateInMillis;

    private SparseArray<Fragment.SavedState> savedStateSA = new SparseArray<>();
    private @Nullable SubjectData eventData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleUtil.setConfigLang(this);
        setContentView(R.layout.activity_attendee_event_details);
        overridePendingTransition(R.anim.zoom_in, R.anim.none);

        if (savedInstanceState!=null){
            eventId = savedInstanceState.getInt("eventId");
            eventData = savedInstanceState.getParcelable("eventData");
            eventDateInMillis  = savedInstanceState.getLong("eventDateInMillis");

        } else {
            Bundle extras = getIntent().getExtras();
            if (extras!=null) {
                eventId = extras.getInt("eventId", eventId);
                eventData = extras.getParcelable("eventData");
                eventDateInMillis = extras.getLong("eventDate", System.currentTimeMillis());
            }
        }

        setupUI();
        setupTabFragments(savedInstanceState);
        populateEventDetails();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult()!=null) {
                        String deviceToken = task.getResult().getToken();
//                        if (DEBUG_MODE) { //TODO REMOVE DEBUG
                            Log.d("IIT",deviceToken);
//                        }
                    }
                });
    }

    private void setupUI() {
        setTitle(R.string.activity_event_details_title);

        Toolbar toolbar = findViewById(R.id.aevent_details_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        tabLayout = findViewById(R.id.aevent_details_tabLy);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                swapFragments(tab.getPosition(), getFragmentTag(tab.getPosition()));
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        appBarLy = findViewById(R.id.aevent_details_appbarLy);

        //Swipe to refresh layout setup
        mSwipeRefreshLayout = findViewById(R.id.aevent_details_swipe_refreshLy);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isDataRefreshing) {
                isDataRefreshing = true;
                refreshEventDetails();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
    }

    private void populateEventDetails() {
        TextView nameTv = findViewById(R.id.aevent_details_nameTv);
        TextView descTv = findViewById(R.id.aevent_details_descTv);
        TextView dateTv = findViewById(R.id.aevent_details_dateTv);

        nameTv.setText(TextFormatter.formatAsHTML(String.format("<u>%s</u>", eventData.getName())));
        descTv.setText(eventData.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
        dateTv.setText(sdf.format(new Date(eventDateInMillis)));
    }

    private void refreshEventDetails() {
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onHostGetEventSuccess(SubjectData event) {
                    super.onHostGetEventSuccess(event);
                    eventData = event;
                    populateEventDetails();
                    refreshEventDetailsFrag();

                    showInfoToast(getString(R.string.msg_updated), Toast.LENGTH_SHORT);
                }

                @Override
                public void onComplete(boolean failed, Integer failCode) {
                    isDataRefreshing = false;
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (failed) {
                        if (failCode== Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), 5000);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }
                    }
                }
            });
        rb.checkInternet(this).attachToken(this, TOKEN_ACCESS);
        rb.callRequest(() -> rb.hostGetEventById(eventId));
    }

    private void refreshEventDetailsFrag() {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_event_details_tag));
        if (frag instanceof EventDetailsFrag) {
            ((EventDetailsFrag)frag).onParentRefresh(eventData);
        }
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
        bundle.putLong("eventDateInMillis", eventDateInMillis+(24*60*60*1000)-1000);
        bundle.putParcelable("eventData", eventData);
        switch (tabIdx) {
            case 0:fragment = new EventDetailsFrag(); break;
            default: throw new RuntimeException("AttendeeEventDetailsActivity:createFragment No fragment found for tabIdx: " + tabIdx);
        }

        fragment.setArguments(bundle);
        fragment.setInitialSavedState(savedStateSA.get(tabIdx));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.aevent_details_contentLy, fragment, tag)
                .commit();
    }

    private void saveFragmentState(int tabIdx) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.aevent_details_contentLy);
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
            default: throw new RuntimeException("AttendeeEventDetailsActivity:getFragmentTag No fragment tag found for tabIdx: " + tabIdx);
        }
    }

    private void showInfoSnackbar(String msg, int duration) {
        if (getWindow().getDecorView().isShown()) {
            if (!TextUtils.isEmpty(msg)) {
                Snackbar.make(findViewById(R.id.aevent_details_snackbarLy), msg, duration).show();
            }
        }
    }

    private void showInfoToast(String msg, int duration) {
        if (getWindow().getDecorView().isShown()) {
            if (!TextUtils.isEmpty(msg)) {
                Toast.makeText(this, msg, duration).show();
            }
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (!mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.setEnabled(i == 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appBarLy.addOnOffsetChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appBarLy.removeOnOffsetChangedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("eventId", eventId);
        outState.putLong("eventDateInMillis", eventDateInMillis);
        outState.putInt("currentTabIdx", currentTabIdx);
        outState.putSparseParcelableArray("savedStateSA", savedStateSA);
        outState.putParcelable("eventData", eventData);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.none, R.anim.zoom_out);
    }
}
