package com.sse.iamhere.Server.Body;

import com.google.gson.annotations.SerializedName;

public class CheckData {
    private final String manager;
    private final String host;

    @SerializedName(value="participator")
    private final String attendee;

    public CheckData(String manager, String host, String attendee) {
        this.manager = manager;
        this.host = host;
        this.attendee = attendee;
    }

    public boolean isRegistered() {
        return (found(manager) || found(host) || found(attendee));
    }

    public boolean getManagerRegisterStatus() {
        return found(manager);
    }

    public boolean getHostRegisterStatus() {
        return found(host);
    }

    public boolean getAttendeeRegisterStatus() {
        return found(attendee);
    }

    private boolean found(String role) {
        return role.equals("Found");
    }
}
