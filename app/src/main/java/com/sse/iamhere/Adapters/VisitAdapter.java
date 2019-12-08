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
import com.sse.iamhere.Utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.VisitHolder> {
    private List<VisitData> visits = new ArrayList<>();

    public interface VisitAdapterListener {
        void onClick(int eventId);
    }

    private VisitAdapterListener visitAdapterListener;
    public VisitAdapter(VisitAdapterListener val) {
        this.visitAdapterListener = val;
    }

    public void setVisits(List<VisitData> visits) {
        this.visits = visits;
        notifyDataSetChanged();
    }

    class VisitHolder extends RecyclerView.ViewHolder {
        private TextView dateTv;
        private TextView eventNameTv;

        VisitHolder(View itemView) {
            super(itemView);
            this.dateTv = itemView.findViewById(R.id.visit_dateTv);
            this.eventNameTv = itemView.findViewById(R.id.visit_eventNameTv);

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
    public VisitHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visit, parent, false);

        return new VisitHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitHolder holder, int pos) {
        final VisitData currentVisit = visits.get(pos);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentVisit.getDate());

        String date = CalendarUtil.getStringDate(calendar, holder.dateTv.getContext());
        holder.dateTv.setText(date);

        holder.eventNameTv.setText(currentVisit.getEventName());
    }

    @Override
    public int getItemCount() {
        return visits.size();
    }
}
