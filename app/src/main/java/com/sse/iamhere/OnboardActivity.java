package com.sse.iamhere;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;
import com.sse.iamhere.Utils.LocaleUtil;

import java.util.ArrayList;
import java.util.List;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class OnboardActivity extends AhoyOnboarderActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.setConfigLang(this);
        super.onCreate(savedInstanceState);

        int activityOrientation = ORIENTATION_PORTRAIT;

        if (getIntent().getExtras()!=null) {
            activityOrientation = getIntent().getExtras().getInt("activityOrientation", activityOrientation);
        }

        if (activityOrientation ==ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        } else if (activityOrientation ==ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        } else throw new RuntimeException("Authentication Activity: supplied bad orientation");

        setupOnboard();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setupOnboard() {
        AhoyOnboarderCard card1 = new AhoyOnboarderCard(
                getString(R.string.onboard_manager_label),
                getString(R.string.onboard_manager_desc_label),
                R.drawable.onboard_manager);
        card1.setBackgroundColor(R.color.black_transparent_image);
        card1.setTitleColor(R.color.white);
        card1.setDescriptionColor(R.color.grey_200);
        card1.setTitleTextSize(dpToPixels(12, this));
        card1.setDescriptionTextSize(dpToPixels(6, this));

        AhoyOnboarderCard card2 = new AhoyOnboarderCard(
                getString(R.string.onboard_host_label),
                getString(R.string.onboard_host_desc_label),
                R.drawable.onboard_host);
        card2.setBackgroundColor(R.color.black_transparent_image);
        card2.setTitleColor(R.color.white);
        card2.setDescriptionColor(R.color.grey_200);
        card2.setTitleTextSize(dpToPixels(12, this));
        card2.setDescriptionTextSize(dpToPixels(6, this));

        AhoyOnboarderCard card3 = new AhoyOnboarderCard(
                getString(R.string.onboard_attendee_label),
                getString(R.string.onboard_attendee_desc_label),
                R.drawable.onboard_attendee);
        card3.setBackgroundColor(R.color.black_transparent_image);
        card3.setTitleColor(R.color.white);
        card3.setDescriptionColor(R.color.grey_200);
        card3.setTitleTextSize(dpToPixels(12, this));
        card3.setDescriptionTextSize(dpToPixels(5, this));


        List<AhoyOnboarderCard> pages = new ArrayList<>();
        pages.add(card1);
        pages.add(card2);
        pages.add(card3);

        showNavigationControls(true);
        setInactiveIndicatorColor(R.color.grey_300);
        setActiveIndicatorColor(R.color.black);

        setFinishButtonTitle(getString(R.string.onboard_done_label));
        setFinishButtonDrawableStyle(ContextCompat.getDrawable(OnboardActivity.this, R.drawable.rounded_button));

        List<Integer> colors = new ArrayList<>();
        colors.add(R.color.colorManagerPrimary);
        colors.add(R.color.colorHostPrimary);
        colors.add(R.color.colorAttendeePrimary);
        setColorBackground(colors);
        setOnboardPages(pages);
    }

    @Override
    public void onFinishButtonPressed() {
        finish();
//        Toast.makeText(this, "Finish Pressed", Toast.LENGTH_SHORT).show();
    }

}
