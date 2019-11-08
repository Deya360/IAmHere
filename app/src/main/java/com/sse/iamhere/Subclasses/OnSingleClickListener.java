package com.sse.iamhere.Subclasses;

import android.os.SystemClock;
import android.view.View;

/*
* Borrowed from: https://stackoverflow.com/a/20672997/11193085
* */
public abstract class OnSingleClickListener implements View.OnClickListener {
    private long MIN_CLICK_INTERVAL=600;
    private long mLastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;

        mLastClickTime=currentClickTime;

        if(elapsedTime<=MIN_CLICK_INTERVAL)
            return;

        onSingleClick(v);
    }

    public void setMinClickInterval(int interval) {
        MIN_CLICK_INTERVAL = interval;
    }
}