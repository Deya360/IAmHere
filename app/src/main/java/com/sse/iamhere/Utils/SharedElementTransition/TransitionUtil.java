package com.sse.iamhere.Utils.SharedElementTransition;

import android.content.Context;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.animation.AnimationUtils;

import com.sse.iamhere.R;

public class TransitionUtil {
    public static Transition makeEnterTransition(Context context) {
        TransitionSet set = new TransitionSet();

        Transition fade = new Fade();
        set.addTransition(fade);

        Slide slide = new Slide(Gravity.BOTTOM);
        slide.setInterpolator(AnimationUtils.loadInterpolator(context, android.R.interpolator.linear_out_slow_in));
        set.addTransition(slide);

        set.excludeTarget(android.R.id.navigationBarBackground, true);
        set.excludeTarget(android.R.id.statusBarBackground, true);
        set.excludeTarget(R.id.party_details_appbar, true);

        return set;
    }
}
