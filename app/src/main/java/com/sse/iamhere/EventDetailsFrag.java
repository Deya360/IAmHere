package com.sse.iamhere;


import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sse.iamhere.Server.Body.HostBrief;
import com.sse.iamhere.Server.Body.SubjectData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EventDetailsFrag extends Fragment {
    private @Nullable SubjectData subjectData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState!=null){
            subjectData = savedInstanceState.getParcelable("subjectData");

        } else {
            Bundle extras = getArguments();
            if (extras!=null) {
                subjectData = extras.getParcelable("subjectData");
            }
        }

        populateData();
    }

    private void populateData() {
        if (getActivity()==null) {
            Log.e("EventDetailsFrag", "Activity is null populateData");
            return;
        }
        TextView startDateTv = getActivity().findViewById(R.id.event_details_startDateTv);
        TextView finishDateTv = getActivity().findViewById(R.id.event_details_finishDateTv);
        TextView inviteCodeTv = getActivity().findViewById(R.id.event_details_inviteCodeTv);
        TextView hostsTv = getActivity().findViewById(R.id.event_details_hostsTv);

        if (subjectData!=null) {
            setInfoWithFormatting(startDateTv, "Start Date", formatDate(subjectData.getStartDate()));
            setInfoWithFormatting(finishDateTv, "Finish Date", formatDate(subjectData.getFinishDate()));
            setInfoWithFormatting(inviteCodeTv, "Invite Code", subjectData.getCodeWord());
            setInfoWithFormatting(hostsTv, "Hosts", formatHostsList(subjectData.getHosts()));
        }
    }

    private void setInfoWithFormatting(TextView view, String label, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.setText(Html.fromHtml(String.format("<b>%s:</b> %s", label, text), Html.FROM_HTML_MODE_COMPACT), TextView.BufferType.SPANNABLE);
        } else {
            view.setText(Html.fromHtml(String.format("<b>%s:</b> %s", label, text)), TextView.BufferType.SPANNABLE);
        }
    }

    private String formatDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date(date));
    }

    private String formatHostsList(ArrayList<HostBrief> list) {
        String returnStr = "";
        if (list==null || list.isEmpty()) return returnStr;

        for (HostBrief h : list) {
            returnStr += h.getName() + ", ";
        }
        return returnStr.substring(0, returnStr.length()-2);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("subjectData", subjectData);
    }
}
