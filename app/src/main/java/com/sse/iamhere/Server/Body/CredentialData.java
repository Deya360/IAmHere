package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

public class CredentialData implements Parcelable {
    private String name;
    private String email;

    public CredentialData(String name, String email) {
        this.name = name;
        this.email = email;
    }

    protected CredentialData(Parcel in) {
        name = in.readString();
        email = in.readString();
    }

    public CredentialData() {
    }

    public static final Creator<CredentialData> CREATOR = new Creator<CredentialData>() {
        @Override
        public CredentialData createFromParcel(Parcel in) {
            return new CredentialData(in);
        }

        @Override
        public CredentialData[] newArray(int size) {
            return new CredentialData[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
    }
}
