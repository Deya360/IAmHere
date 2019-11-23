package com.sse.iamhere.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sse.iamhere.R;
import com.sse.iamhere.Server.Body.VisitData;
import com.sse.iamhere.Subclasses.OnSingleClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.EventHolder> {
    private List<VisitData> visits = new ArrayList<>();

    public interface VisitAdapterListener {
        void onClick(int attendeeId);
    }

    private VisitAdapterListener visitAdapterListener;
    public VisitAdapter(VisitAdapterListener val) {
        this.visitAdapterListener = val;
    }

    public void setVisits(List<VisitData> visits) {
        this.visits = visits;
        notifyDataSetChanged();
    }

    class EventHolder extends RecyclerView.ViewHolder {
        private TextView nameTv;
        private TextView timesTv;

        EventHolder(View itemView) {
            super(itemView);
            this.nameTv = itemView.findViewById(R.id.visit_nameTv);
            this.timesTv = itemView.findViewById(R.id.visit_timesTv);

            itemView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                visitAdapterListener.onClick(visits.get(getAdapterPosition()).getId());
                }
            });
        }
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visit, parent, false);

        return new EventHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int pos) {
        final VisitData currentVisit = visits.get(pos);

        holder.nameTv.setText(currentVisit.getName());
        holder.timesTv.setText(formatTimesList(currentVisit.getVisits()));
    }

    private String formatTimesList(ArrayList<Long> list) {
        String returnStr = "";
        if (list==null || list.isEmpty()) return returnStr;

        for (Long l : list) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());

            returnStr += sdf.format(new Date(l)) + ", ";
        }
        return returnStr.substring(0, returnStr.length()-2);
    }

    @Override
    public int getItemCount() {
        return visits.size();
    }
}
