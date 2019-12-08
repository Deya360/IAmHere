package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

public class ParticipatorBrief implements Parcelable {
    private String id;
    private String name;

    public ParticipatorBrief(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected ParticipatorBrief(Parcel in) {
        id = in.readString();
        name = in.readString();
    }

    public static final Creator<SubjectBrief> CREATOR = new Creator<SubjectBrief>() {
        @Override
        public SubjectBrief createFromParcel(Parcel in) {
            return new SubjectBrief(in);
        }

        @Override
        public SubjectBrief[] newArray(int size) {
            return new SubjectBrief[size];
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
