package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

public class PartyBrief implements Parcelable {
    private String id;
    private String name;

    public PartyBrief(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected PartyBrief(Parcel in) {
        id = in.readString();
        name = in.readString();
    }

    public static final Creator<PartyBrief> CREATOR = new Creator<PartyBrief>() {
        @Override
        public PartyBrief createFromParcel(Parcel in) {
            return new PartyBrief(in);
        }

        @Override
        public PartyBrief[] newArray(int size) {
            return new PartyBrief[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
    }
}
