package com.sse.iamhere.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Feed implements Parcelable {
    private ArrayList<FeedItem> items = new ArrayList<>();

    public Feed() {
    }

    public Feed(ArrayList<FeedItem> items) {
        this.items = items;
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public FeedItem getTopItem() {
        if (!items.isEmpty()) return items.get(0);
        return null;
    }

    public boolean addItem(FeedItem item) {
        if (!items.contains(item)) {
            items.add(item);
            return true;
        }
        return false;
    }

    public boolean removeItem(FeedItem item) {
        return items.remove(item);
    }



    protected Feed(Parcel in) {
        items = in.createTypedArrayList(FeedItem.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(items);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Feed> CREATOR = new Creator<Feed>() {
        @Override
        public Feed createFromParcel(Parcel in) {
            return new Feed(in);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };
}
