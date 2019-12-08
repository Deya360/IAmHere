package com.sse.iamhere.Adapters.VisitIViewHolders;

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.os.ConfigurationCompat;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.sse.iamhere.R;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class VisitIHeaderViewHolder extends ParentViewHolder {
    private TextView headerTv;
    private TextView headerCountTv;
    private ImageView arrowIv;

    public VisitIHeaderViewHolder(View itemView) {
        super(itemView);
        headerTv = itemView.findViewById(R.id.item_visitI_headerTv);
        headerCountTv = itemView.findViewById(R.id.item_visitI_header_countTv);
        arrowIv = itemView.findViewById(R.id.item_visitI_header_arrowIv);
    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        if (expanded) {
            animateCollapse(arrowIv, 300);

        } else {
            animateExpand(arrowIv, 300);
        }
    }

    public void onBind(VisitIHeaderItem group) {
        headerTv.setText(group.getPartyName());

        int childCount = group.getChildList().size();
        headerCountTv.setText(String.format(ConfigurationCompat.getLocales(headerCountTv.getContext().getResources().getConfiguration()).get(0),
                                            "• %d", childCount));

        if (isExpanded()) {
            animateExpand(arrowIv, 0);
        } else {
            animateCollapse(arrowIv, 0);
        }
    }

    private void animateExpand(View view, int duration) {
        RotateAnimation rotate =
                new RotateAnimation(0, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(duration);
        rotate.setFillAfter(true);
        view.startAnimation(rotate);
    }

    private void animateCollapse(View view, int duration) {
        RotateAnimation rotate =
                new RotateAnimation(180, 0, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(duration);
        rotate.setFillAfter(true);
        view.startAnimation(rotate);
    }

    @Override
    public boolean shouldItemViewClickToggleExpansion() {
        return true;
    }
}
