package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class HostBrief implements Parcelable {
    private String id;
    private String name;

    public HostBrief(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected HostBrief(Parcel in) {
        id = in.readString();
        name = in.readString();
    }

    public static final Creator<HostBrief> CREATOR = new Creator<HostBrief>() {
        @Override
        public HostBrief createFromParcel(Parcel in) {
            return new HostBrief(in);
        }

        @Override
        public HostBrief[] newArray(int size) {
            return new HostBrief[size];
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostBrief hostBrief = (HostBrief) o;
        return Objects.equals(id, hostBrief.id) &&
                Objects.equals(name, hostBrief.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
