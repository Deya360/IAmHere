package com.sse.iamhere;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.sse.iamhere.Adapters.EventAdapter;
import com.sse.iamhere.Dialogs.DatePickerFragment;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Server.RequestBuilder;
import com.sse.iamhere.Server.RequestsCallback;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Subclasses.RepeatListener;
import com.sse.iamhere.Utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import static com.sse.iamhere.Utils.CalendarUtil.getNextDay;
import static com.sse.iamhere.Utils.CalendarUtil.getPrvDay;
import static com.sse.iamhere.Utils.CalendarUtil.getStringDate;
import static com.sse.iamhere.Utils.CalendarUtil.isSameDay;
import static com.sse.iamhere.Utils.CalendarUtil.trimTime;
import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;

public class EventsFrag extends Fragment {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EmptySupportedRecyclerView recyclerView;
    private ShimmerFrameLayout placeholderLy;
    private FrameLayout contentLy;

    private Button datePrvIv;
    private Button dateNxtIv;
    private TextView dateStrTv;

    private Calendar currentDate;
    private EventAdapter adapter;
    private Parcelable savedRecyclerLayoutState;

    private boolean isDataRefreshing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity()==null) {
            Log.e("EventsFrag", "Activity is null onCreate");
            return;
        }

        if (getArguments()!=null) {

            setArguments(null);
        }

        if (savedInstanceState!=null){
            currentDate = Calendar.getInstance();
            currentDate.setTimeInMillis(savedInstanceState.getLong("currentDate"));
            savedRecyclerLayoutState = savedInstanceState.getParcelable("mListState");
        }

        setupUI();
        initDatePanel(view);
        populateEvents(currentDate.getTime());
    }


    private void setupUI() {
        Objects.requireNonNull(getActivity()).setTitle(R.string.fragment_events_title);

        // Setup of swipe to refresh layout
        mSwipeRefreshLayout = getActivity().findViewById(R.id.events_swipe_refreshLy);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isDataRefreshing) {
                isDataRefreshing = true;
                populateEvents(currentDate.getTime());
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        // Initial setup of recycler view
        recyclerView = getActivity().findViewById(R.id.events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(new EventAdapter.EventAdapterListener() {
            @Override
            public void onClick(SubjectData eventData, int eventId) {
                // on click of item in the events list
                startEventDetailActivity(eventData, eventId);
            }
        });
        recyclerView.setAdapter(adapter);

        placeholderLy = getActivity().findViewById(R.id.events_placeholderLy);
        contentLy = getActivity().findViewById(R.id.events_contentLy);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initDatePanel(@NonNull View view) {
        datePrvIv = view.findViewById(R.id.events_date_prvBtn);
        dateNxtIv = view.findViewById(R.id.events_date_nxtBtn);
        dateStrTv = view.findViewById(R.id.events_date_dateStrTv);

        dateStrTv.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Calendar today = Calendar.getInstance();
                selectDate(trimTime(today));
                populateEvents(currentDate.getTime());
            }
        });

        datePrvIv.setOnTouchListener(new RepeatListener(300, 125, v -> {
            datePrvIv.playSoundEffect(SoundEffectConstants.CLICK);
            selectDate(getPrvDay(currentDate));
            recyclerView.removeAllViewsInLayout();
            populateEvents(currentDate.getTime());
        }));

        dateNxtIv.setOnTouchListener(new RepeatListener(300, 125, v -> {
            datePrvIv.playSoundEffect(SoundEffectConstants.CLICK);
            selectDate(getNextDay(currentDate));
            recyclerView.removeAllViewsInLayout();
            populateEvents(currentDate.getTime());
        }));

        ImageView dateCalTv = view.findViewById(R.id.events_date_calIv);
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
//            deActivateArrow(datePrvIv);
            activateArrow(dateNxtIv);

        } else {
            activateArrow(datePrvIv);
        }
    }

    private void activateArrow(Button view) {
        ViewCompat.setBackgroundTintList(
                view,
                null);
        view.setClickable(true);
        view.setFocusable(true);
        view.setEnabled(true);
    }
    private void deActivateArrow(Button view) {
        ViewCompat.setBackgroundTintList(
                view,
                ColorStateList.valueOf(Color.parseColor("#77616161")));
        view.setClickable(false);
        view.setFocusable(false);
        view.setEnabled(false);
    }

    /*
    * Callback method for the DatePickerDialog, sets the local currentDate to the user picked date
    * */
    public void onClose(Calendar selectedDate) {
        if (selectedDate!=null) {
            selectDate(trimTime(selectedDate));
            populateEvents(currentDate.getTime());
        }
    }


    private void populateEvents(Date date) {
        showShimmer();

        RequestBuilder rb = new RequestBuilder()
            .setCallback(new RequestsCallback() {
                @Override
                public void onHostGetEventsByDateSuccess(Set<SubjectData> eventData) {
                    super.onHostGetEventsByDateSuccess(eventData);
                    adapter.setEvents(new ArrayList<>(eventData));
                    if (recyclerView.getLayoutManager() != null && savedRecyclerLayoutState != null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                    if (eventData.isEmpty()) {
                        recyclerView.setPlaceHolderView(getActivity().findViewById(R.id.events_empty_view));
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
                            showInfoSnackbar(getString(R.string.splash_connectionTv_label), 9000);

                        } else {
                            showInfoSnackbar(getString(R.string.msg_server_error), 5000);
                        }
                    }
                    hideShimmer();
                }
            });
        rb.checkInternet(getContext()).attachToken(getContext(), TOKEN_ACCESS);
        rb.callRequest(() -> rb.hostGetEventsByDate(date.getTime()));
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
                    Snackbar.make(getActivity().findViewById(R.id.home_mainLy), msg, duration).show();
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

    private void startEventDetailActivity(SubjectData eventData, int eventId) {
        Bundle bundle = new Bundle();
        bundle.putInt("eventId", eventId);
        bundle.putParcelable("eventData", eventData);
        bundle.putLong("eventDate", currentDate.getTimeInMillis());
        Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("currentDate", currentDate.getTimeInMillis());
        RecyclerView.LayoutManager mListState = recyclerView.getLayoutManager();
        if (mListState!=null) {
            outState.putParcelable("mListState", mListState.onSaveInstanceState());
        }
    }
}
