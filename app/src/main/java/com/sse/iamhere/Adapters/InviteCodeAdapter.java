package com.sse.iamhere.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sse.iamhere.R;

import java.util.ArrayList;
import java.util.List;

public class InviteCodeAdapter extends RecyclerView.Adapter<InviteCodeAdapter.InviteCodeHolder> {
    private List<String> inviteCodes = new ArrayList<>();

    public InviteCodeAdapter() {

    }

    public void setInviteCodes(List<String> individuals) {
        this.inviteCodes = individuals;
        notifyDataSetChanged();
    }

    public void removeInviteCodeAt(int position) {
        inviteCodes.remove(position);
        notifyItemRemoved(position);
    }

    class InviteCodeHolder extends RecyclerView.ViewHolder {
        private TextView nameTv;

        InviteCodeHolder(View itemView) {
            super(itemView);
            this.nameTv = itemView.findViewById(R.id.invite_code_nameTv);
        }
    }

    @NonNull
    @Override
    public InviteCodeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite_code, parent, false);

        return new InviteCodeHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteCodeHolder holder, int pos) {
        final String currentInviteCode = inviteCodes.get(pos);

        holder.nameTv.setText(currentInviteCode);
    }

    @Override
    public int getItemCount() {
        return inviteCodes.size();
    }
}
