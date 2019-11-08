package com.sse.iamhere;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.sse.iamhere.Adapters.EventAdapter;
import com.sse.iamhere.Data_depreciated.Entitites.Subject;
import com.sse.iamhere.Data_depreciated.VM.SubjectViewModel;
import com.sse.iamhere.Dialogs.DatePickerFragment;
import com.sse.iamhere.Subclasses.EmptySupportedRecyclerView;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Subclasses.RepeatListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

import static com.sse.iamhere.Utils.CalendarUtils.getNextDay;
import static com.sse.iamhere.Utils.CalendarUtils.getPrvDay;
import static com.sse.iamhere.Utils.CalendarUtils.getStringDate;
import static com.sse.iamhere.Utils.CalendarUtils.isSameDay;
import static com.sse.iamhere.Utils.CalendarUtils.trimTime;

public class EventsFrag extends Fragment {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EmptySupportedRecyclerView recyclerView;
    private Parcelable savedRecyclerLayoutState;

    private Button datePrvIv;
    private Button dateNxtIv;
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
            currentDate = Calendar.getInstance();
            currentDate.setTimeInMillis(savedInstanceState.getLong("currentDate"));
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

        Subject[] subjectz = {
            new Subject("Accounting & Finance", "cool description" ,"07112019"),
            new Subject("Business & Management Studies", "not cool description" , "07112019"),
            new Subject("Law", "" , "07112019"),
            new Subject("Business & Management Studies", "random very very long description that seems to never ever ever end, you thought that was it, nope, yup, it keeps on going, and on, and on, and on....." , "08112019"),
            new Subject("Management Studies", "" , "08112019")
        };

        subjectViewModel.insert(subjectz)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableSingleObserver<long[]>() {
                @Override
                public void onSuccess(long[] longs) {

                }

                @Override
                public void onError(Throwable e) {

                }
            });

        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        dateFormat.format(currentDate.getTime());

        subjectViewModel.getAllByDate(dateFormat.format(currentDate.getTime()))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .filter(subjects -> {
                adapter.setSubjects(subjects);
                mSwipeRefreshLayout.setRefreshing(false);
                if (recyclerView.getLayoutManager()!=null && savedRecyclerLayoutState!=null)
                    recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                if(subjects.isEmpty()) {
                    recyclerView.setPlaceHolderView(getActivity().findViewById(R.id.events_empty_view));
                }

                return !subjects.isEmpty();
            }).subscribe(new DisposableSubscriber<List<Subject>>() {
            @Override
            public void onNext(List<Subject> subjects) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
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


    @SuppressLint("ClickableViewAccessibility")
    private void initDatePanel(@NonNull View view) {
        datePrvIv = view.findViewById(R.id.events_date_prvBtn);
        dateNxtIv = view.findViewById(R.id.events_date_nxtBtn);
        dateStrTv = view.findViewById(R.id.events_date_dateStrTv);

        datePrvIv.setOnTouchListener(new RepeatListener(300, 125, v -> {
            selectDate(getPrvDay(currentDate));
            populateTestData(currentDate.getTime());
        }));

        dateNxtIv.setOnTouchListener(new RepeatListener(300, 125, v -> {
            selectDate(getNextDay(currentDate));
            populateTestData(currentDate.getTime());
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
        Calendar cal;

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
        dateStrTv.setText(getStringDate(date));

        if (isSameDay(date, Calendar.getInstance())) {
            deActivateArrow(datePrvIv);
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
        }
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
