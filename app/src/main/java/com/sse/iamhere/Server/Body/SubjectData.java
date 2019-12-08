package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SubjectData implements Parcelable {
    @SerializedName("id")
    private Integer id;

    @SerializedName("name")
    private String name;

    @SerializedName("plan")
    private Integer plan;

    @SerializedName("description")
    private String description;

    @SerializedName("start_date")
    private long startDate;

    @SerializedName("finish_date")
    private long finishDate;

    @SerializedName("code")
    private String codeWord;

    @SerializedName("hosts")
    private ArrayList<HostBrief> hosts;

    @SerializedName("parties")
    private ArrayList<PartyBrief> parties;

    public SubjectData(Integer subjectId, String name, Integer plan, String description, long startDate, long finishDate,
                       String codeWord, ArrayList<HostBrief> hosts, ArrayList<PartyBrief> parties) {
        this.id = subjectId;
        this.name = name;
        this.plan = plan;
        this.description = description;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.codeWord = codeWord;
        this.hosts = hosts;
        this.parties = parties;
    }

    protected SubjectData(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        if (in.readByte() == 0) {
            plan = null;
        } else {
            plan = in.readInt();
        }
        description = in.readString();
        startDate = in.readLong();
        finishDate = in.readLong();
        codeWord = in.readString();
        hosts = in.createTypedArrayList(HostBrief.CREATOR);
        parties = in.createTypedArrayList(PartyBrief.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(name);
        if (plan == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(plan);
        }
        dest.writeString(description);
        dest.writeLong(startDate);
        dest.writeLong(finishDate);
        dest.writeString(codeWord);
        dest.writeTypedList(hosts);
        dest.writeTypedList(parties);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubjectData> CREATOR = new Creator<SubjectData>() {
        @Override
        public SubjectData createFromParcel(Parcel in) {
            return new SubjectData(in);
        }

        @Override
        public SubjectData[] newArray(int size) {
            return new SubjectData[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPlan() {
        return plan;
    }

    public void setPlan(Integer plan) {
        this.plan = plan;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(long finishDate) {
        this.finishDate = finishDate;
    }

    public String getCodeWord() {
        return codeWord;
    }

    public void setCodeWord(String codeWord) {
        this.codeWord = codeWord;
    }

    public ArrayList<HostBrief> getHosts() {
        return hosts;
    }

    public void setHosts(ArrayList<HostBrief> hosts) {
        this.hosts = hosts;
    }

    public ArrayList<PartyBrief> getParties() {
        return parties;
    }

    public void setParties(ArrayList<PartyBrief> parties) {
        this.parties = parties;
    }

}
