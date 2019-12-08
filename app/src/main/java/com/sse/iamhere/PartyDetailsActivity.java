package com.sse.iamhere;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.SharedElementEnterCallback;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.LocaleUtil;
import com.sse.iamhere.Utils.MaterialColors700;
import com.sse.iamhere.Utils.SharedElementTransition.TransitionUtil;
import com.sse.iamhere.Utils.TextFormatter;

import java.util.Objects;

import static com.sse.iamhere.Utils.Constants.DEBUG_MODE;
import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class PartyDetailsActivity extends AppCompatActivity {
    private SharedElementEnterCallback sharedElementCallback;
    private @Nullable PartyData partyData;
    private int partyId = -1;
    private int itemPos = -1;

    private ImageView memberCountIv;
    private TextView memberCountTv;
    private TextView nameTv;
    private TextView descTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleUtil.setConfigLang(this);
        setContentView(R.layout.activity_party_details);
        supportPostponeEnterTransition();

        getWindow().setEnterTransition(TransitionUtil.makeEnterTransition(this));

        initViews();
        sharedElementCallback = new SharedElementEnterCallback(getIntent(), memberCountTv, nameTv, descTv);

        if (savedInstanceState!=null){
            partyId = savedInstanceState.getInt("partyId");
            partyData = savedInstanceState.getParcelable("partyData");
            itemPos = savedInstanceState.getInt("itemPos");
            sharedElementCallback.onRestoreInstanceState(savedInstanceState.getParcelableArrayList("sharedElementCallback"));

        } else {
            Bundle extras = getIntent().getExtras();
            if (extras!=null) {
                partyId = extras.getInt("partyId", partyId);
                partyData = extras.getParcelable("partyData");
                itemPos = extras.getInt("itemPos", itemPos);
            }
        }
        setEnterSharedElementCallback(sharedElementCallback);

        setupUI();
        populatePartyDetails();
        supportStartPostponedEnterTransition();
    }

    private void setupUI() {
        setTitle(R.string.activity_party_details_title);

        Toolbar toolbar = findViewById(R.id.party_details_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Swipe to refresh layout setup
//        mSwipeRefreshLayout = findViewById(R.id.event_details_swipe_refreshLy);
//        mSwipeRefreshLayout.setOnRefreshListener(() -> {
//            if (!isDataRefreshing) {
//                isDataRefreshing = true;
//                refreshEventDetails();
//            }
//        });
//
//        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
//                                                    android.R.color.holo_green_dark,
//                                                    android.R.color.holo_orange_dark,
//                                                    android.R.color.holo_blue_dark);
    }

    private void initViews() {
        memberCountIv = findViewById(R.id.party_details_member_countIv);
        memberCountTv = findViewById(R.id.party_details_member_countTv);
        nameTv = findViewById(R.id.party_details_nameTv);
        descTv = findViewById(R.id.party_details_descTv);
    }

    private void populatePartyDetails() {
        ViewCompat.setBackgroundTintList(
                memberCountIv,
                ColorStateList.valueOf(MaterialColors700.toArr()[itemPos%19]));

        memberCountTv.setText(TextFormatter.prettyCount(partyData.getAttendeeCount()));
        nameTv.setText(partyData.getPartyName());
        descTv.setText(partyData.getDescription());
    }

    private void showLeavePartyConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.action_leave_party_label));
        builder.setMessage(getString(R.string.action_leave_dlg_msg));
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            leaveParty(partyData.getPartyId());
        });
        builder.show();
    }

    private void leaveParty(String partyId) {
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()!=null) {
                    String deviceToken = task.getResult().getToken();

                    if (!TextUtils.isEmpty(deviceToken)) {
                        leaveParty(Integer.parseInt(partyId), deviceToken);
                    } else {
                        leaveParty(Integer.parseInt(partyId), "");
                    }

                    if (DEBUG_MODE) { //TODO REMOVE DEBUG
                        Log.d("IIT", task.getResult().getToken());
                    }
                }
            });
    }

    private void leaveParty(int id, String deviceToken) {
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
        rb.callRequest(() -> rb.attendeeLeaveParty(id, deviceToken));
    }


    private void showInfoSnackbar(String msg, int duration) {
        if (getWindow().getDecorView().isShown()) {
            if (!TextUtils.isEmpty(msg)) {
                Snackbar.make(findViewById(android.R.id.content), msg, duration).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_party_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_leave_party:
                showLeavePartyConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("partyId", partyId);
        outState.putParcelable("partyData", partyData);
        outState.putInt("itemPos", itemPos);
        outState.putParcelableArrayList("sharedElementCallback", sharedElementCallback.onSaveInstanceState());
    }
}
