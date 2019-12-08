package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PartyDataExtra implements Parcelable {
    @SerializedName("id")
    private final String id;

    @SerializedName("manager_id")
    private final String managerId;

    @SerializedName("name")
    private final String name;

    @SerializedName("description")
    private final String description;

    @SerializedName("code")
    private final String codeWord;

    @SerializedName("subjects")
    private final ArrayList<PartyBrief> events;

    @SerializedName("participators")
    private final ArrayList<PartyBrief> attendees;

    @SerializedName("participator_count")
    private final String attendeeCount;

    public PartyDataExtra(String id, String managerId, String name,
                          String description, String codeWord, ArrayList<PartyBrief> events,
                          ArrayList<PartyBrief> attendees, String attendeeCount) {
        this.id = id;
        this.managerId = managerId;
        this.name = name;
        this.description = description;
        this.codeWord = codeWord;
        this.events = events;
        this.attendees = attendees;
        this.attendeeCount = attendeeCount;
    }

    public String getId() {
        return id;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCodeWord() {
        return codeWord;
    }

    public ArrayList<PartyBrief> getEvents() {
        return events;
    }

    public ArrayList<PartyBrief> getAttendees() {
        return attendees;
    }

    public String getAttendeeCount() {
        return attendeeCount;
    }



    protected PartyDataExtra(Parcel in) {
        id = in.readString();
        managerId = in.readString();
        name = in.readString();
        description = in.readString();
        codeWord = in.readString();
        events = in.createTypedArrayList(PartyBrief.CREATOR);
        attendees = in.createTypedArrayList(PartyBrief.CREATOR);
        attendeeCount = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(managerId);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(codeWord);
        dest.writeTypedList(events);
        dest.writeTypedList(attendees);
        dest.writeString(attendeeCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PartyDataExtra> CREATOR = new Creator<PartyDataExtra>() {
        @Override
        public PartyDataExtra createFromParcel(Parcel in) {
            return new PartyDataExtra(in);
        }

        @Override
        public PartyDataExtra[] newArray(int size) {
            return new PartyDataExtra[size];
        }
    };
}
