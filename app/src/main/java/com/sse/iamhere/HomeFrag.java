package com.sse.iamhere;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.EventAdapter;
import com.sse.iamhere.Dialogs.DatePickerFragment;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Utils.Constants;
import com.sse.iamhere.Utils.PermissionUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static com.sse.iamhere.Utils.CalendarUtil.getStringDate;
import static com.sse.iamhere.Utils.CalendarUtil.isSameDay;
import static com.sse.iamhere.Utils.CalendarUtil.trimTime;
import static com.sse.iamhere.Utils.Constants.SCANNER_RQ;
import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class HomeFrag extends Fragment {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EmptySupportedRecyclerView recyclerView;
    private ShimmerFrameLayout placeholderLy;
    private FrameLayout contentLy;

    private TextView dateStrTv;

    private Calendar currentDate;
    private EventAdapter adapter;
    private Parcelable savedRecyclerLayoutState;

    private boolean isDataRefreshing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity()==null) {
            Log.e("HomeFrag", "Activity is null onCreate");
            return;
        }

        if (getArguments()!=null) {

            setArguments(null);
        }

        if (savedInstanceState!=null){
//            currentDate = Calendar.getInstance();
//            currentDate.setTimeInMillis(savedInstanceState.getLong("currentDate"));
            savedRecyclerLayoutState = savedInstanceState.getParcelable("mListState");
        }

        setupUI();
        initDatePanel(view);
        populateData(currentDate.getTime());
    }


    private void setupUI() {
        Objects.requireNonNull(getActivity()).setTitle(R.string.fragment_home_title);

        // Setup of swipe to refresh layout
        mSwipeRefreshLayout = getActivity().findViewById(R.id.homef_swipe_refreshLy);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isDataRefreshing) {
                isDataRefreshing = true;
                populateData(currentDate.getTime());
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        // Initial setup of recycler view
        recyclerView = getActivity().findViewById(R.id.homef_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(new EventAdapter.EventAdapterListener() {
            @Override
            public void onClick(SubjectData eventData, int eventId) {
//                 on click of item in the events list
                startAttendeeEventDetailActivity(eventData, eventId);
            }
        });
        recyclerView.setAdapter(adapter);

        placeholderLy = getActivity().findViewById(R.id.homef_placeholderLy);
        contentLy = getActivity().findViewById(R.id.homef_contentLy);

        FloatingActionButton fab = getActivity().findViewById(R.id.homef_fab);
        fab.setOnClickListener(view -> startQRScannerActivity());
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initDatePanel(@NonNull View view) {
        dateStrTv = view.findViewById(R.id.homef_date_dateStrTv);

        dateStrTv.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Calendar today = Calendar.getInstance();
                selectDate(trimTime(today));
                populateData(currentDate.getTime());
            }
        });

        ImageView dateCalTv = view.findViewById(R.id.homef_date_calIv);
        dateCalTv.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                showDatePickerDialog();
            }
        });

        if (dateStrTv.getText()==null || TextUtils.isEmpty(dateStrTv.getText().toString())) {
            if (currentDate==null) {
                Calendar today = Calendar.getInstance();
                selectDate(trimTime(today));
            } else {
                selectDate(currentDate);
            }
        }
    }

    private void showDatePickerDialog() {
        SublimeOptions options = new SublimeOptions();

        options.setCanPickDateRange(false);
        options.setDateParams(currentDate.get(Calendar.YEAR),
                              currentDate.get(Calendar.MONTH),
                              currentDate.get(Calendar.DAY_OF_MONTH));

        options.setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER);

        Bundle bundle = new Bundle();
        bundle.putParcelable("SUBLIME_OPTIONS", options);

        DatePickerFragment datePickerFrag = new DatePickerFragment();
        datePickerFrag.setCancelable(true);
        datePickerFrag.setArguments(bundle);
        datePickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        datePickerFrag.show(getChildFragmentManager(),
                            getResources().getString(R.string.fragment_date_picker_dialog_tag));
    }

    private void selectDate(Calendar date) {
        currentDate = date;
        dateStrTv.setText(getStringDate(date, getActivity()));

        if (isSameDay(date, Calendar.getInstance())) {
            dateStrTv.setText(getString(R.string.today));
        }
    }

    /*
     * Callback method for the DatePickerDialog, sets the local currentDate to the user picked date
     * */
    public void onClose(Calendar selectedDate) {
        if (selectedDate!=null) {
            selectDate(trimTime(selectedDate));
            populateData(currentDate.getTime());
        }
    }

    private void populateData(Date date) {
        showShimmer();

        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onAttendeeGetEventsByDateSuccess(Set<SubjectData> eventData) {
                    super.onAttendeeGetEventsByDateSuccess(eventData);
                    adapter.setEvents(new ArrayList<>(eventData));
                    if (recyclerView.getLayoutManager() != null && savedRecyclerLayoutState != null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                    if (eventData.isEmpty()) {
                        recyclerView.setPlaceHolderView(getActivity().findViewById(R.id.homef_empty_view));
                    }

                    if (getActivity()!=null)
                        showInfoToast(getString(R.string.msg_updated), Toast.LENGTH_SHORT);
                }

                @Override
                public void onComplete(boolean failed, Integer failCode) {
                    isDataRefreshing = false;
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (failed && getActivity()!=null) {
                        if (failCode== Constants.RQM_EC.NO_INTERNET_CONNECTION) {
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), 5000);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }

                    } else {
                        hideShimmer();
                    }
                }
            });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);
        rb.callRequest(() -> {rb.attendeeGetEventsByDate(date.getTime()); });
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
        if (getActivity()!=null) {
            if (getActivity().getWindow().getDecorView().isShown()) {
                if (!TextUtils.isEmpty(msg)) {
                    Snackbar.make(getActivity().findViewById(R.id.homef_mainLy), msg, duration).show();
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

    private void startAttendeeEventDetailActivity(SubjectData eventData, int eventId) {
        Bundle bundle = new Bundle();
        bundle.putInt("eventId", eventId);
        bundle.putInt("eventId", eventId);
        bundle.putParcelable("eventData", eventData);
        bundle.putLong("eventDate", currentDate.getTimeInMillis());
        Intent intent = new Intent(getActivity(), AttendeeEventDetailsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    private void startQRScannerActivity() {
        if (!(getActivity() instanceof HomeActivity)) {
            Log.w("HomeFrag", "Activity is not Home Activity startQRScannerActivity:PermissionUtil:onNeedPermissions");
            return;
        }

        HomeActivity activity = (HomeActivity)getActivity();

        PermissionUtil.checkPermission(
                getActivity(), new String[]{Manifest.permission.CAMERA}, new PermissionUtil.PermissionAskListener() {
                    @Override
                    public void onNeedPermissions(ArrayList<String> permissions) {
                        ActivityCompat.requestPermissions(activity,
                                                          new String[]{Manifest.permission.CAMERA}, Constants.PERMISSIONS_RQ);

                        new Handler().postDelayed(() -> {
                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                                    == PackageManager.PERMISSION_GRANTED) {
                                onPermissionGranted(permissions.get(0));
                            }
                        }, 1000);
                    }

                    @Override
                    public void onPermissionPreviouslyDenied(String permission) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle(getString(R.string.permission_title));
                        alertDialog.setIcon(R.drawable.ic_info_outline_light_black_24dp);
                        alertDialog.setMessage(getString(R.string.permission_message));
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                                              (dialog, which) -> {
                                                  dialog.dismiss();
                                                  ActivityCompat.requestPermissions(activity,
                                                                                    new String[]{Manifest.permission.CAMERA}, Constants.PERMISSIONS_RQ);
                                              });
                        alertDialog.show();
                    }

                    @Override
                    public void onPermissionDisabled(String permission) {
                        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.homef_mainLy),
                                                          getString(R.string.permission_disabled_msg), 5000);
                        snackbar.setAction(getString(R.string.permission_disabled_action), v -> {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }).show();
                    }

                    @Override
                    public void onPermissionGranted(String permission) {
                        Intent myIntent = new Intent(activity, ScannerActivity.class);
                        startActivityForResult(myIntent, SCANNER_RQ);
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // receives result from scanner activity
        if (requestCode== Constants.SCANNER_RQ) {
            if (resultCode==RESULT_OK) {
                if (data!=null) {
                    boolean updateNeeded = data.getBooleanExtra("updateNeeded", false);
                    if (updateNeeded) {
                        populateData(currentDate.getTime());
                    }
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
