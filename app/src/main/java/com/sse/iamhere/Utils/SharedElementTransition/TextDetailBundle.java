package com.sse.iamhere.Utils.SharedElementTransition;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

public class TextDetailBundle implements Parcelable {
    private float textSize;
    private Integer currentTextColor;
    private Rect padding;

    public TextDetailBundle(TextView textView) {
        textSize = textView.getTextSize();
        currentTextColor = textView.getCurrentTextColor();
        padding = new Rect(textView.getPaddingLeft(),
                           textView.getPaddingTop(),
                           textView.getPaddingRight(),
                           textView.getPaddingBottom());
    }

    public float getTextSize() {
        return textSize;
    }

    public Integer getCurrentTextColor() {
        return currentTextColor;
    }

    public Rect getPadding() {
        return padding;
    }



    protected TextDetailBundle(Parcel in) {
        textSize = in.readFloat();
        if (in.readByte() == 0) {
            currentTextColor = null;
        } else {
            currentTextColor = in.readInt();
        }
        padding = in.readParcelable(Rect.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(textSize);
        if (currentTextColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(currentTextColor);
        }
        dest.writeParcelable(padding, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TextDetailBundle> CREATOR = new Creator<TextDetailBundle>() {
        @Override
        public TextDetailBundle createFromParcel(Parcel in) {
            return new TextDetailBundle(in);
        }

        @Override
        public TextDetailBundle[] newArray(int size) {
            return new TextDetailBundle[size];
        }
    };
}
