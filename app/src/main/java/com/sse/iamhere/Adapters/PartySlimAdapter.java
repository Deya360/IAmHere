package com.sse.iamhere.Adapters;

import android.content.res.ColorStateList;
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
import com.sse.iamhere.Server.Body.PartyBrief;
import com.sse.iamhere.Subclasses.OnSingleClickListener;
import com.sse.iamhere.Utils.MaterialColors700;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PartySlimAdapter extends RecyclerView.Adapter<PartySlimAdapter.PartySlimHolder> {
    private List<PartyBrief> parties = new ArrayList<>();

    public interface PartySlimAdapterListener {
        void onClick(int partyId);
    }

    private PartySlimAdapterListener partySlimAdapterListener;
    public PartySlimAdapter(PartySlimAdapterListener psal) {
        this.partySlimAdapterListener = psal;
    }

    public void setParties(List<PartyBrief> parties) {
        this.parties = parties;
        notifyDataSetChanged();
    }

    class PartySlimHolder extends RecyclerView.ViewHolder {
        private ImageView memberCountIv;
        private TextView memberCountTv;
        private TextView nameTv;

        PartySlimHolder(View itemView) {
            super(itemView);
            this.memberCountIv = itemView.findViewById(R.id.party_slim_member_countIv);
            this.memberCountTv = itemView.findViewById(R.id.party_slim_member_countTv);
            this.nameTv = itemView.findViewById(R.id.party_slim_nameTv);

            itemView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    partySlimAdapterListener.onClick(Integer.parseInt(parties.get(getAdapterPosition()).getId()));
                }
            });
        }
    }

    @NonNull
    @Override
    public PartySlimHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_party_slim, parent, false);

        return new PartySlimHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PartySlimHolder holder, int pos) {
        final PartyBrief currentParty = parties.get(pos);

        int mcl = currentParty.getAttendeeCount().length();
        if (mcl<=2) {
            holder.memberCountTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);

        } else if (mcl<=3) {
            holder.memberCountTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);

        } else { // > 3
            holder.memberCountTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        }
        holder.memberCountTv.setText(prettyCount(currentParty.getAttendeeCount()));

        ViewCompat.setBackgroundTintList(
                holder.memberCountIv,
                ColorStateList.valueOf(MaterialColors700.toArr()[pos%19]));

        holder.nameTv.setText(currentParty.getName());
    }

    private String prettyCount(String count) {
        Number number = Integer.valueOf(count);
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

    @Override
    public int getItemCount() {
        return parties.size();
    }
}
