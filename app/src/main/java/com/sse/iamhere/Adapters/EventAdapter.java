package com.sse.iamhere.Adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sse.iamhere.R;
import com.sse.iamhere.Server.Body.SubjectData;
import com.sse.iamhere.Subclasses.OnSingleClickListener;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventHolder> {
    private List<SubjectData> events = new ArrayList<>();

    public interface EventAdapterListener {
        void onClick(SubjectData eventData, int eventId);
    }

    private EventAdapterListener eventAdapterListener;
    public EventAdapter(EventAdapterListener eal) {
        this.eventAdapterListener = eal;
    }

    public void setEvents(List<SubjectData> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    class EventHolder extends RecyclerView.ViewHolder {
        private TextView nameTv;
        private TextView descTv;

        EventHolder(View itemView) {
            super(itemView);
            this.nameTv = itemView.findViewById(R.id.event_nameTv);
            this.descTv = itemView.findViewById(R.id.event_descTv);

            itemView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                eventAdapterListener.onClick(events.get(getAdapterPosition()), events.get(getAdapterPosition()).getId());
                }
            });
        }
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);

        return new EventHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int pos) {
        final SubjectData currentEvent = events.get(pos);

        holder.nameTv.setText(currentEvent.getName());

        String description = currentEvent.getDescription();
        if (!TextUtils.isEmpty(description)) {
            holder.descTv.setVisibility(View.VISIBLE);
            holder.descTv.setText(description);
        } else {
            holder.descTv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
