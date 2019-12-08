package com.sse.iamhere;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sse.iamhere.Dialogs.SendAnnouncementDialog;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.LocaleUtil;
import com.sse.iamhere.Utils.TextFormatter;

import net.glxn.qrgen.android.QRCode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class EventDetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AppBarLayout appBarLy;
    private TabLayout tabLayout;

    private boolean isDataRefreshing = false;
    private boolean isTakingAttendance = false;
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
        setContentView(R.layout.activity_event_details);
        overridePendingTransition(R.anim.zoom_in, R.anim.none);

        if (savedInstanceState!=null){
            eventId = savedInstanceState.getInt("eventId");
            isTakingAttendance = savedInstanceState.getBoolean("isTakingAttendance", isTakingAttendance);
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
        if (DateUtils.isToday(eventDateInMillis)) generateQRCode();
    }

    private void setupUI() {
        setTitle(R.string.activity_event_details_title);

        Toolbar toolbar = findViewById(R.id.event_details_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        int visibility = View.VISIBLE;
        if (!DateUtils.isToday(eventDateInMillis)) visibility = View.GONE;
        findViewById(R.id.event_details_dragView).setVisibility(visibility);

        tabLayout = findViewById(R.id.event_details_tabLy);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                swapFragments(tab.getPosition(), getFragmentTag(tab.getPosition()));
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        appBarLy = findViewById(R.id.event_details_appbarLy);

        //Swipe to refresh layout setup
        mSwipeRefreshLayout = findViewById(R.id.event_details_swipe_refreshLy);
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

        SlidingUpPanelLayout slidingLy = findViewById(R.id.event_details_slidingLy);
        slidingLy.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                mSwipeRefreshLayout.setEnabled(slideOffset==0 || isTakingAttendance);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                isTakingAttendance = (newState==SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });
    }

    private void populateEventDetails() {
        TextView nameTv = findViewById(R.id.event_details_nameTv);
        TextView descTv = findViewById(R.id.event_details_descTv);
        TextView dateTv = findViewById(R.id.event_details_dateTv);

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
                            return;

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }
                    }

                    refreshVisitsIFrag();
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

    private void refreshVisitsIFrag() {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_visits_inner_tag));
        if (frag instanceof VisitsIFrag) {
            ((VisitsIFrag)frag).onParentRefresh(eventData);
        }
    }

    private void generateQRCode() {
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onQRCodeSuccess(String qrcode) {
                    super.onQRCodeSuccess(qrcode);
                    Log.d("QRCODE", qrcode); //TODO: remove temp debug
                    Bitmap myBitmap = QRCode.from(qrcode)
                            .withSize(350, 350).bitmap();

                    ImageView myImage = findViewById(R.id.event_details_qrCodeIv);
                    myImage.setImageBitmap(myBitmap);
                }

                @Override
                public void onFailure(int errorCode) {
                    super.onFailure(errorCode);
                    if (errorCode == Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                        showInfoSnackbar(getString(R.string.splash_connectionTv_label), 5000);

                    } else {
                        showInfoSnackbar(getString(R.string.msg_server_error), 5000);
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
        bundle.putLong("eventDateInMillis", eventDateInMillis+(24*60*60*1000)-1000);
        bundle.putParcelable("eventData", eventData);
        switch (tabIdx) {
            case 0:
                fragment = new EventDetailsFrag(); break;
            case 1: fragment = new VisitsIFrag(); break;
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
            case 1: return getString(R.string.fragment_visits_inner_tag);
            default: throw new RuntimeException("EventDetailsActivity:getFragmentTag No fragment tag found for tabIdx: " + tabIdx);
        }
    }

    private void showAnnouncementDialog() {
        SendAnnouncementDialog sendAnnouncementDialog = new SendAnnouncementDialog(eventId);
        sendAnnouncementDialog.show(getSupportFragmentManager(), getString(R.string.fragment_send_announcement_dialog_tag));
    }

    private void showLeaveEventConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.action_leave_event_label));
        builder.setMessage(getString(R.string.action_leave_dlg_msg));
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            leaveEvent(eventData.getId());
        });
        builder.show();
    }

    private void leaveEvent(Integer id) {
        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onComplete(boolean failed, Integer failCode) {
                    if (failed) {
                        if (failCode== Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), 5000);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }

                    } else {
                        finish(); //todo make this activity get started by onActivityResult, here return flag need update rv list
                    }
                }
            });
        rb.checkInternet(this).attachToken(this, TOKEN_ACCESS);
        rb.callRequest(() -> rb.hostLeaveEvent(id));
    }

    private void showInfoSnackbar(String msg, int duration) {
        if (getWindow().getDecorView().isShown()) {
            if (!TextUtils.isEmpty(msg)) {
                Snackbar.make(findViewById(R.id.event_details_snackbarLy), msg, duration).show();
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
            mSwipeRefreshLayout.setEnabled(i==0 || isTakingAttendance);
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
    public void onBackPressed() {
        if (isTakingAttendance && preventBackCount > 0) {
            showInfoSnackbar("Attendance is in progress, if you return, it will be stopped", 3000); //todo: temp
            preventBackCount--;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_announcement:
                showAnnouncementDialog();
                return true;

            case R.id.action_leave_event:
                showLeaveEventConfirmationDialog();
                return true;

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
        outState.putBoolean("isTakingAttendance", isTakingAttendance);
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
