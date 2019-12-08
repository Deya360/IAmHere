package com.sse.iamhere.Adapters;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sse.iamhere.R;
import com.sse.iamhere.Server.Body.PartyData;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Utils.MaterialColors700;

import java.util.ArrayList;
import java.util.List;

import static com.sse.iamhere.Utils.TextFormatter.prettyCount;

public class PartyAdapter extends RecyclerView.Adapter<PartyAdapter.PartyHolder> {
    private List<PartyData> parties = new ArrayList<>();

    public interface PartyAdapterListener {
        void onClick(int partyId, PartyData partyData, int pos,
            View itemLayout, ImageView memberCountIv, TextView memberCountTv, TextView nameTv, TextView descTv);
    }

    private PartyAdapterListener partyAdapterListener;
    public PartyAdapter(PartyAdapterListener pal) {
        this.partyAdapterListener = pal;
    }

    public void setParties(List<PartyData> parties) {
        this.parties = parties;
        notifyDataSetChanged();
    }

    class PartyHolder extends RecyclerView.ViewHolder {
        private ImageView memberCountIv;
        private TextView memberCountTv;
        private TextView nameTv;
        private TextView descTv;

        PartyHolder(View itemView) {
            super(itemView);
            this.memberCountIv = itemView.findViewById(R.id.party_member_countIv);
            this.memberCountTv = itemView.findViewById(R.id.party_member_countTv);
            this.nameTv = itemView.findViewById(R.id.party_nameTv);
            this.descTv = itemView.findViewById(R.id.party_descTv);

            itemView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                partyAdapterListener.onClick(
                        Integer.parseInt(parties.get(getAdapterPosition()).getPartyId()),
                        parties.get(getAdapterPosition()), getAdapterPosition(),
                        itemView, memberCountIv, memberCountTv, nameTv, descTv);
                }
            });
        }
    }

    @NonNull
    @Override
    public PartyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_party, parent, false);

        return new PartyHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PartyHolder holder, int pos) {
        final PartyData currentParty = parties.get(pos);

        int mcl = currentParty.getAttendeeCount().length();
        if (mcl<=3) {
            holder.memberCountTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,29);

        } else { // > 3
            holder.memberCountTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,23);
        }
        holder.memberCountTv.setText(prettyCount(currentParty.getAttendeeCount()));

        ViewCompat.setBackgroundTintList(
                holder.memberCountIv,
                ColorStateList.valueOf(MaterialColors700.toArr()[pos%19]));

        holder.nameTv.setText(currentParty.getPartyName());

        String description = currentParty.getDescription();
        if (!TextUtils.isEmpty(description)) {
            holder.descTv.setVisibility(View.VISIBLE);
            holder.descTv.setText(description);
        } else {
            holder.descTv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return parties.size();
    }
}
