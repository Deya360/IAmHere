package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PartyBrief implements Parcelable {
    private String id;
    private String name;

    @SerializedName("participator_count")
    private String attendeeCount;

    @SerializedName("participator_visit_times")
    private ArrayList<VisitIData> attendeesVisits;

    public PartyBrief(String id, String name, String attendeeCount) {
        this.id = id;
        this.name = name;
        this.attendeeCount = attendeeCount;
    }

    public PartyBrief(int String, String name, String attendeeCount, ArrayList<VisitIData> attendeesVisits) {
        this.id = id;
        this.name = name;
        this.attendeeCount = attendeeCount;
        this.attendeesVisits = attendeesVisits;
    }


    protected PartyBrief(Parcel in) {
        id = in.readString();
        name = in.readString();
        attendeeCount = in.readString();
        attendeesVisits = in.createTypedArrayList(VisitIData.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(attendeeCount);
        dest.writeTypedList(attendeesVisits);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getAttendeeCount() {
        return attendeeCount;
    }

    public void setAttendeeCount(String attendeeCount) {
        this.attendeeCount = attendeeCount;
    }


    public ArrayList<VisitIData> getAttendeesVisits() {
        return attendeesVisits;
    }

    public void setAttendeesVisits(ArrayList<VisitIData> attendeesVisits) {
        this.attendeesVisits = attendeesVisits;
    }



}
