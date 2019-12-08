package com.sse.iamhere.Adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sse.iamhere.R;
import com.sse.iamhere.Server.Body.HostBrief;
import com.sse.iamhere.Subclasses.OnSingleClickListener;

import java.util.ArrayList;
import java.util.List;

public class HostSlimAdapter extends RecyclerView.Adapter<HostSlimAdapter.HostSlimHolder> {
    private List<HostBrief> hosts = new ArrayList<>();

    public interface HostSlimAdapterListener {
        void onClick(int hostId);
    }

    private HostSlimAdapterListener hostSlimAdapterListener;
    public HostSlimAdapter(HostSlimAdapterListener hsal) {
        this.hostSlimAdapterListener = hsal;
    }

    public void setHosts(List<HostBrief> hosts) {
        for (HostBrief h : hosts) {
            if (TextUtils.isEmpty(h.getName())) hosts.remove(h);
        }
        this.hosts = hosts;
        notifyDataSetChanged();
    }

    class HostSlimHolder extends RecyclerView.ViewHolder {
        private TextView nameTv;

        HostSlimHolder(View itemView) {
            super(itemView);
            this.nameTv = itemView.findViewById(R.id.host_slim_nameTv);

            itemView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                hostSlimAdapterListener.onClick(Integer.parseInt(hosts.get(getAdapterPosition()).getId()));
                }
            });
        }
    }

    @NonNull
    @Override
    public HostSlimHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_host_slim, parent, false);

        return new HostSlimHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HostSlimHolder holder, int pos) {
        final HostBrief currentHost = hosts.get(pos);

        holder.nameTv.setText(currentHost.getName());
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }
}
