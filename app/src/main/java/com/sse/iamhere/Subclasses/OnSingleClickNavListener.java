package com.sse.iamhere.Subclasses;

import android.os.SystemClock;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;

public abstract class OnSingleClickNavListener implements NavigationView.OnNavigationItemSelectedListener {
    private long MIN_CLICK_INTERVAL=400;
    private long mLastClickTime;

    public abstract boolean onSingleNavigationItemSelected(@NonNull MenuItem menuItem);

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;

        mLastClickTime=currentClickTime;

        if(elapsedTime<=MIN_CLICK_INTERVAL) return false;

        return onSingleNavigationItemSelected(menuItem);
    }

    public void setMinClickInterval(int interval) {
        MIN_CLICK_INTERVAL = interval;
    }
}