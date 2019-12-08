package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class VisitIData implements Parcelable {
    private int id;
    private String name;
    private ArrayList<Long> visits;

    public VisitIData(int id, String name, ArrayList<Long> visits) {
        this.id = id;
        this.name = name;
        this.visits = visits;
    }


    protected VisitIData(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VisitIData> CREATOR = new Creator<VisitIData>() {
        @Override
        public VisitIData createFromParcel(Parcel in) {
            return new VisitIData(in);
        }

        @Override
        public VisitIData[] newArray(int size) {
            return new VisitIData[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Long> getVisits() {
        return visits;
    }

    public void setVisits(ArrayList<Long> visits) {
        this.visits = visits;
    }
}
