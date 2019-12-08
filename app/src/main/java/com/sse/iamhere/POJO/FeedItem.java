package com.sse.iamhere.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class FeedItem implements Parcelable {
    private String id; //event id or party id
    private String msg;

    public FeedItem(String id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public String getId() {
        return id;
    }

    public Integer getIId() {
        return Integer.valueOf(id);
    }

    public String getMsg() {
        return msg;
    }



    protected FeedItem(Parcel in) {
        id = in.readString();
        msg = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(msg);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FeedItem> CREATOR = new Creator<FeedItem>() {
        @Override
        public FeedItem createFromParcel(Parcel in) {
            return new FeedItem(in);
        }

        @Override
        public FeedItem[] newArray(int size) {
            return new FeedItem[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedItem item = (FeedItem) o;
        return id.equals(item.id) && msg.equals(item.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, msg);
    }
}
