package com.sse.iamhere.Subclasses;

import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sse.iamhere.Utils.SharedElementTransition.TextDetailBundle;

import java.util.ArrayList;
import java.util.List;

public class SharedElementEnterCallback extends SharedElementCallback {
    private final Intent intent;
    private final TextView memberCountTv;
    private TextDetailBundle memberCountTvTargetBundle;

    private final TextView nameTv;
    private TextDetailBundle nameTvTargetBundle;

    private final TextView descTv;
    private TextDetailBundle descTvTargetBundle;


    public SharedElementEnterCallback(Intent intent, TextView memberCountTv, TextView nameTv, TextView descTv) {
        this.intent = intent;
        this.memberCountTv = memberCountTv;
        this.nameTv = nameTv;
        this.descTv = descTv;
    }

    @Override
    public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
        //Store target TV details
        memberCountTvTargetBundle = new TextDetailBundle(memberCountTv);
        nameTvTargetBundle = new TextDetailBundle(nameTv);
        descTvTargetBundle = new TextDetailBundle(descTv);

        //Set intent TV details
        setBundleDetails(memberCountTv, intent.getParcelableExtra("memberCountTvDetailBundle"));
        setBundleDetails(nameTv, intent.getParcelableExtra("nameTvDetailBundle"));
        setBundleDetails(descTv, intent.getParcelableExtra("descTvDetailBundle"));
    }

    private void setBundleDetails(TextView textView, TextDetailBundle textDetailBundle) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textDetailBundle.getTextSize());
        textView.setTextColor(textDetailBundle.getCurrentTextColor());

        Rect padding = textDetailBundle.getPadding();
        textView.setPadding(padding.left, padding.top, padding.right, padding.bottom);
    }

    @Override
    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
        restoreBundleDetails(memberCountTv, memberCountTvTargetBundle);
        restoreBundleDetails(nameTv, nameTvTargetBundle);
        restoreBundleDetails(descTv, descTvTargetBundle);
    }

    private void restoreBundleDetails(TextView textView, TextDetailBundle textDetailBundle) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textDetailBundle.getTextSize());

        if (textDetailBundle.getCurrentTextColor() != null) {
            textView.setTextColor(textDetailBundle.getCurrentTextColor());
        }

        Rect padding = textDetailBundle.getPadding();
        if (padding != null) {
            textView.setPadding(padding.left, padding.top, padding.right, padding.bottom);
        }
    }

    public void onRestoreInstanceState(@Nullable ArrayList<TextDetailBundle> textViewDetailBundles) {
        if (textViewDetailBundles!=null) {
            memberCountTvTargetBundle = textViewDetailBundles.get(0);
            nameTvTargetBundle = textViewDetailBundles.get(1);
            descTvTargetBundle = textViewDetailBundles.get(2);
        }
    }

    public ArrayList<TextDetailBundle> onSaveInstanceState() {
        return new ArrayList<TextDetailBundle>() {{
            add(memberCountTvTargetBundle);
            add(nameTvTargetBundle);
            add(descTvTargetBundle);
        }};
    }
}