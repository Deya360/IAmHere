package com.sse.iamhere.Views;

import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;

/*
* Borrowed from: https://stackoverflow.com/a/20672997/11193085
* */
public abstract class OnSingleClickAdapterViewListener implements AdapterView.OnItemClickListener {
    private long MIN_CLICK_INTERVAL=400;
    private long mLastClickTime;

    public abstract void onSingleItemClick(AdapterView<?> parent, View view, int position, long id);


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;

        mLastClickTime=currentClickTime;

        if(elapsedTime<=MIN_CLICK_INTERVAL)
            return;

        onSingleItemClick(parent, view, position, id);
    }

    public void setMinClickInterval(int interval) {
        MIN_CLICK_INTERVAL = interval;
    }
}