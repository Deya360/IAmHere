package com.sse.iamhere.Dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.appeaser.sublimepickerlibrary.SublimePicker;
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.sse.iamhere.EventsFrag;
import com.sse.iamhere.HomeFrag;
import com.sse.iamhere.R;
import com.sse.iamhere.VisitsFrag;

public class DatePickerFragment extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getActivity()==null) {
            Log.e("DatePickerFragment", "getActivity() is null");
            return null;
        }

        SublimePicker mSublimePicker = (SublimePicker) getActivity().getLayoutInflater()
                                                    .inflate(R.layout.sublime_picker, container);
        if (getArguments() != null) {
            SublimeOptions options = getArguments().getParcelable("SUBLIME_OPTIONS");

            mSublimePicker.initializePicker(options, new SublimeListenerAdapter() {
                @Override
                public void onDateTimeRecurrenceSet(SublimePicker sublimeMaterialPicker,
                                                    SelectedDate selectedDate, int hourOfDay, int minute,
                                                    SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                                    String recurrenceRule) {

                    if (getParentFragment() instanceof EventsFrag) {
                        try {
                            ((EventsFrag) getParentFragment()).onClose(selectedDate.getStartDate());
                        } catch (Exception e) {
                            throw new ClassCastException("EventsFrag must implement DatePickerDialogListener");
                        }
                    } else if (getParentFragment() instanceof HomeFrag) {
                        try {
                            ((HomeFrag) getParentFragment()).onClose(selectedDate.getStartDate());
                        } catch (Exception e) {
                            throw new ClassCastException("HomeFrag must implement DatePickerDialogListener");
                        }

                    } else if (getParentFragment() instanceof VisitsFrag) {
                        try {
                            ((VisitsFrag) getParentFragment()).onClose(selectedDate.getStartDate());
                        } catch (Exception e) {
                            throw new ClassCastException("HomeFrag must implement DatePickerDialogListener");
                        }

                    } else {
                        throw new ClassCastException("DatePickerDialogListener only works with EventsFrag or HomeFrag");
                    }
                    dismiss();
                }

                @Override
                public void onCancelled() {
                    dismiss();
                }
            });
        }
        return mSublimePicker;
    }
}