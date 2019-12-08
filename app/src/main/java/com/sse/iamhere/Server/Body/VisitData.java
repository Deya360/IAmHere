package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class VisitData implements Parcelable {
    @SerializedName("visit_id")
    private Integer id;

    @SerializedName("timestamp")
    private long date;

    @SerializedName("participator_id")
    private Integer attendeeId;

    @SerializedName("participator_name")
    private String attendeeName;

    @SerializedName("host_id")
    private Integer hostId;

    @SerializedName("host_name")
    private String hostName;

    @SerializedName("subject_id")
    private Integer eventId;

    @SerializedName("subject_name")
    private String eventName;


    public VisitData(Integer id, long date, Integer attendeeId, String attendeeName,
                     Integer hostId, String hostName, Integer eventId, String eventName) {
        this.id = id;
        this.date = date;
        this.attendeeId = attendeeId;
        this.attendeeName = attendeeName;
        this.hostId = hostId;
        this.hostName = hostName;
        this.eventId = eventId;
        this.eventName = eventName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public Integer getAttendeeId() {
        return attendeeId;
    }

    public void setAttendeeId(Integer attendeeId) {
        this.attendeeId = attendeeId;
    }

    public String getAttendeeName() {
        return attendeeName;
    }

    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }



    protected VisitData(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        date = in.readLong();
        if (in.readByte() == 0) {
            attendeeId = null;
        } else {
            attendeeId = in.readInt();
        }
        attendeeName = in.readString();
        if (in.readByte() == 0) {
            hostId = null;
        } else {
            hostId = in.readInt();
        }
        hostName = in.readString();
        if (in.readByte() == 0) {
            eventId = null;
        } else {
            eventId = in.readInt();
        }
        eventName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeLong(date);
        if (attendeeId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(attendeeId);
        }
        dest.writeString(attendeeName);
        if (hostId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(hostId);
        }
        dest.writeString(hostName);
        if (eventId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(eventId);
        }
        dest.writeString(eventName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VisitData> CREATOR = new Creator<VisitData>() {
        @Override
        public VisitData createFromParcel(Parcel in) {
            return new VisitData(in);
        }

        @Override
        public VisitData[] newArray(int size) {
            return new VisitData[size];
        }
    };

}
