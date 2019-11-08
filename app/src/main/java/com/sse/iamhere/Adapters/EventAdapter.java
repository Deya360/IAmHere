package com.sse.iamhere.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sse.iamhere.Data_depreciated.Entitites.Subject;
import com.sse.iamhere.R;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventHolder> {
    private List<Subject> subjects = new ArrayList<>();
    private String query = "";

    public interface EventAdapterListener {
        void onClick(int subjectId);
    }

    private EventAdapterListener eventAdapterListener;
    public EventAdapter(EventAdapterListener eal) {
        this.eventAdapterListener = eal;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
        notifyDataSetChanged();
    }

    class EventHolder extends RecyclerView.ViewHolder {
        private TextView nameTv;

        EventHolder(View itemView) {
            super(itemView);
            this.nameTv = itemView.findViewById(R.id.event_nameTv);

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
        final Subject currentResult = subjects.get(pos);

        holder.nameTv.setText(currentResult.getName());

    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }
}
