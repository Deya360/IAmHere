package com.sse.iamhere.Adapters.VisitIViewHolders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.core.os.ConfigurationCompat;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.sse.iamhere.R;
import com.sse.iamhere.Subclasses.OnSingleClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class VisitIViewHolder extends ChildViewHolder {
    private TextView nameTv;
    private TextView timesTv;
    private VisitAdapterListener visitAdapterListener;

    public interface VisitAdapterListener {
        void onVisitClick(int attendeeId);
    }

    public VisitIViewHolder(View itemView, VisitAdapterListener val) {
        super(itemView);
        nameTv = itemView.findViewById(R.id.item_visitI_nameTv);
        timesTv = itemView.findViewById(R.id.item_visitI_timesTv);
        this.visitAdapterListener = val;
    }

    public void onBind(VisitIItem visitItem) {
        itemView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
            visitAdapterListener.onVisitClick(visitItem.getVisit().getId());
            }
        });

        nameTv.setText(visitItem.getVisit().getName());
        timesTv.setText(formatTimesList(visitItem.getVisit().getVisits(), timesTv.getContext()));
    }

    private String formatTimesList(ArrayList<Long> list, Context context) {
        StringBuilder returnStr = new StringBuilder();
        if (list==null || list.isEmpty()) return returnStr.toString();

        for (Long l : list) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm",
                                ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0));
            sdf.setTimeZone(TimeZone.getDefault());

            returnStr.append(sdf.format(new Date(l))).append(", ");
        }
        return returnStr.substring(0, returnStr.length()-2);
    }
}
