package com.sse.iamhere.Server.Body;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CredentialData implements Parcelable {
    private String name;
    private String email;

    @SerializedName("email_secured")
    private String hideEmail;

    public CredentialData(String name, String email, String hideEmail) {
        this.name = name;
        this.email = email;
        this.hideEmail = hideEmail;
    }

    public CredentialData() {
    }

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

    public String isEmailHidden() {
        return hideEmail;
    }

    public void setEmailVisibility(String hideEmail) {
        this.hideEmail = hideEmail;
    }



    protected CredentialData(Parcel in) {
        name = in.readString();
        email = in.readString();
        hideEmail = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(hideEmail);
    }

    @Override
    public int describeContents() {
        return 0;
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

}
