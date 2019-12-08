package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PartyData implements Parcelable {
    @SerializedName("party_id")
    private final String partyId;

    @SerializedName("party_name")
    private final String partyName;

    @SerializedName("manager_name")
    private final String managerName;

    @SerializedName("description")
    private final String description;

    @SerializedName("participator_count")
    private final String attendeeCount;

    @SerializedName("broadcast_code")
    private final String codeWord;

    public PartyData(String partyId, String partyName, String managerName, String description, String attendeeCount, String codeWord) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.managerName = managerName;
        this.description = description;
        this.attendeeCount = attendeeCount;
        this.codeWord = codeWord;
    }

    public String getPartyId() {
        return partyId;
    }

    public String getPartyName() {
        return partyName;
    }

    public String getManagerName() {
        return managerName;
    }

    public String getDescription() {
        return description;
    }

    public String getAttendeeCount() {
        return attendeeCount;
    }

    public String getCodeWord() {
        return codeWord;
    }


    protected PartyData(Parcel in) {
        partyId = in.readString();
        partyName = in.readString();
        managerName = in.readString();
        description = in.readString();
        attendeeCount = in.readString();
        codeWord = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(partyId);
        dest.writeString(partyName);
        dest.writeString(managerName);
        dest.writeString(description);
        dest.writeString(attendeeCount);
        dest.writeString(codeWord);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PartyData> CREATOR = new Creator<PartyData>() {
        @Override
        public PartyData createFromParcel(Parcel in) {
            return new PartyData(in);
        }

        @Override
        public PartyData[] newArray(int size) {
            return new PartyData[size];
        }
    };
}
