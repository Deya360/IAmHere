package com.sse.iamhere;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sse.iamhere.Adapters.EventAdapter;
import com.sse.iamhere.Data_depreciated.VM.SubjectViewModel;
import com.sse.iamhere.Views.EmptySupportedRecyclerView;
import com.sse.iamhere.Views.OnSingleClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventsFrag extends Fragment implements DatePickerDialog.OnDateSetListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EmptySupportedRecyclerView recyclerView;
    private Parcelable savedRecyclerLayoutState;

    private ImageView datePrvIv;
    private ImageView dateNxtIv;
    private TextView dateTodayTv;
    private TextView dateTomorrowTv;
    private TextView dateStrTv;

    private Calendar currentDate;
    private EventAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        //Swipe to refresh layout setup
        mSwipeRefreshLayout = rootView.findViewById(R.id.events_swipe_refreshLy);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    populateTestData(currentDate.getTime());
                }
                //TODO: implement
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        return rootView;
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
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(savedInstanceState.getLong("currentDate"));
            currentDate =  cal;

            savedRecyclerLayoutState = savedInstanceState.getParcelable("mListState");
        }

        setupUI();
        initDatePanel(view);
        populateTestData(currentDate.getTime());
    }

    /*
    * Temporary method to populate list from local database //
    * TODO: link with server using http request
    * */
    private void populateTestData(Date date) {
        SubjectViewModel subjectViewModel = ViewModelProviders.of(getActivity()).get(SubjectViewModel.class);

//        subjectViewModel.deleteAll()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new DisposableCompletableObserver() {
//                    @Override
//                    public void onComplete() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//                });

//        Subject[] subjectz = {
//            new Subject("Accounting & Finance", "07112019"),
//            new Subject("Business & Management Studies", "07112019"),
//            new Subject("Law", "07112019"),
//            new Subject("Business & Management Studies", "08112019"),
//            new Subject("Management Studies", "08112019")
//        };
//
//        subjectViewModel.insert(subjectz)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(new DisposableSingleObserver<long[]>() {
//                @Override
//                public void onSuccess(long[] longs) {
//
//                }
//
//                @Override
//                public void onError(Throwable e) {
//
//                }
//            });

//        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
//        dateFormat.format(currentDate.getTime());
//
//        subjectViewModel.getAllByDate(dateFormat.format(currentDate.getTime()))
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .filter(subjects -> {
//                adapter.setSubjects(subjects);
//                mSwipeRefreshLayout.setRefreshing(false);
//                if (recyclerView.getLayoutManager()!=null && savedRecyclerLayoutState!=null)
//                    recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
//                if(subjects.isEmpty()) {
//                    recyclerView.setPlaceHolderView(getActivity().findViewById(R.id.events_empty_view));
//                }
//
//                return !subjects.isEmpty();
//            }).subscribe(new DisposableSubscriber<List<Subject>>() {
//            @Override
//            public void onNext(List<Subject> subjects) {
//            }
//
//            @Override
//            public void onError(Throwable t) {
//            }
//
//            @Override
//            public void onComplete() {
//            }
//        });
    }

    private void setupUI() {
        // Initial setup of recycler view
        recyclerView = getActivity().findViewById(R.id.events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(new EventAdapter.EventAdapterListener() {
            @Override
            public void onClick(int subjectId) {
                // on click of item in the events list
            }
        });
        recyclerView.setAdapter(adapter);

        ProgressBar emptyTextView = getActivity().findViewById(R.id.events_empty_progress_view);
        recyclerView.setEmptyView(emptyTextView);
    }


    private void initDatePanel(@NonNull View view) {
        datePrvIv = view.findViewById(R.id.events_date_prvIv);
        dateNxtIv = view.findViewById(R.id.events_date_nxtIv);
        dateTodayTv = view.findViewById(R.id.events_date_todayTv);
        dateTomorrowTv = view.findViewById(R.id.events_date_tomTv);
        dateStrTv = view.findViewById(R.id.events_date_dateStrTv);

        View.OnClickListener today = v -> {
            EventsFrag.this.selectDate(Calendar.getInstance());
            populateTestData(currentDate.getTime());
        };
        View.OnClickListener tomorrow = v -> {
            Calendar tomorrow1 = Calendar.getInstance();
            tomorrow1.add(Calendar.DAY_OF_YEAR,1);
            selectDate(tomorrow1);
            populateTestData(currentDate.getTime());
        };
        datePrvIv.setOnClickListener(today);
        dateTodayTv.setOnClickListener(today);

        dateNxtIv.setOnClickListener(tomorrow);
        dateTomorrowTv.setOnClickListener(tomorrow);

        ImageView dateCalTv = view.findViewById(R.id.events_date_calIv);
        dateCalTv.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                DatePickerDialog datePickerDialog =
                    new DatePickerDialog(getActivity(), R.style.DatePickerDialogThemeHost, EventsFrag.this,
                        currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        if (dateStrTv.getText()==null || TextUtils.isEmpty(dateStrTv.getText().toString())) {
            selectDate(Calendar.getInstance());
        }
    }

    private void selectDate(Calendar date) {
        currentDate = date;
        dateStrTv.setText(getStringDate(date));

        Calendar today = Calendar.getInstance();
        if (isSameDay(date, today)) {
            deActivateArrow(datePrvIv);
            activateArrow(dateNxtIv);

            selectDay(dateTodayTv);
            deSelectDay(dateTomorrowTv);
            return;
        }

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR,1);
        if (isSameDay(date, tomorrow)) {
            deActivateArrow(dateNxtIv);
            activateArrow(datePrvIv);

            selectDay(dateTomorrowTv);
            deSelectDay(dateTodayTv);

        } else {
            deActivateArrow(datePrvIv);
            deActivateArrow(dateNxtIv);

            deSelectDay(dateTodayTv);
            deSelectDay(dateTomorrowTv);
        }

    }

    private void selectDay(TextView view) {
//        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            1.0f
//        );
//        view.setLayoutParams(param);
        view.setTypeface(view.getTypeface(), Typeface.BOLD);
    }
    private void deSelectDay(TextView view) {
//        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//        view.setLayoutParams(param);
        Typeface t = view.getTypeface();

        view.setTypeface(null, Typeface.NORMAL);
        view.setTypeface(t);
    }

    private void activateArrow(ImageView view) {
        ViewCompat.setBackgroundTintList(
                view,
                null);
        view.setClickable(true);
        view.setFocusable(true);
    }
    private void deActivateArrow(ImageView view) {
        ViewCompat.setBackgroundTintList(
                view,
                ColorStateList.valueOf(Color.parseColor("#77616161")));
        view.setClickable(false);
        view.setFocusable(false);
    }

    private boolean isSameDay(Calendar date1, Calendar date2) {
        return  (date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR) &&
                date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR));
    }

    private String getStringDate(Calendar date) {
//        String weekdays[] = new DateFormatSymbols(Locale.getDefault()).getWeekdays();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        return sdf.format(date.getTime());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar setDate = Calendar.getInstance();
        setDate.set(Calendar.YEAR, year);
        setDate.set(Calendar.MONTH, month);
        setDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        selectDate(setDate);
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

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }
}
