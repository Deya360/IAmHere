package com.sse.iamhere.Views;

import android.view.View;

public abstract class DropdownOnClickListener implements View.OnClickListener {
    private boolean animate = true;

    public abstract void onClick(View v, boolean animate);


    public final void setAnimate(boolean animate) {
        this.animate = animate;
    }

    @Override
    public final void onClick(View v) {
         onClick(v, animate);
    }
}