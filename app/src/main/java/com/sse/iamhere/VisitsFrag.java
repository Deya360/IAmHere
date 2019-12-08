package com.sse.iamhere;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.VisitAdapter;
import com.sse.iamhere.Dialogs.DatePickerFragment;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.Body.VisitData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static com.sse.iamhere.Utils.CalendarUtil.getStringDate;
import static com.sse.iamhere.Utils.CalendarUtil.isSameDay;
import static com.sse.iamhere.Utils.CalendarUtil.trimTime;
import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class VisitsFrag extends Fragment {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EmptySupportedRecyclerView recyclerView;
    private ShimmerFrameLayout placeholderLy;
    private FrameLayout contentLy;

    private TextView dateStrTv;

    private Calendar currentDate;
    private VisitAdapter adapter;
    private Parcelable savedRecyclerLayoutState;

    private boolean isDataRefreshing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visits, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity()==null) {
            Log.e("VisitsFrag", "Activity is null onCreate");
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
        mSwipeRefreshLayout = getActivity().findViewById(R.id.visits_swipe_refreshLy);

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
        recyclerView = getActivity().findViewById(R.id.visits_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new VisitAdapter(new VisitAdapter.VisitAdapterListener() {
            @Override
            public void onClick(int eventId) {

            }
        });
        recyclerView.setAdapter(adapter);

        placeholderLy = getActivity().findViewById(R.id.visits_placeholderLy);
        contentLy = getActivity().findViewById(R.id.visits_contentLy);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initDatePanel(@NonNull View view) {
        dateStrTv = view.findViewById(R.id.visits_date_dateStrTv);

        dateStrTv.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Calendar today = Calendar.getInstance();
                selectDate(trimTime(today));
                populateData(currentDate.getTime());
            }
        });

        ImageView dateCalTv = view.findViewById(R.id.visits_date_calIv);
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
                public void onAttendeeGetVisitsByDateSuccess(Set<VisitData> visitData) {
                    super.onAttendeeGetVisitsByDateSuccess(visitData);

                    adapter.setVisits(new ArrayList<>(visitData));
                    if (recyclerView.getLayoutManager() != null && savedRecyclerLayoutState != null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                    if (visitData.isEmpty()) {
                        recyclerView.setPlaceHolderView(getActivity().findViewById(R.id.visits_empty_view));
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
        rb.callRequest(() -> {rb.attendeeGetVisitsByDate(date.getTime()); });
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
                    Snackbar.make(getActivity().findViewById(R.id.visits_mainLy), msg, duration).show();
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
