package com.example.clockedin;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String token;
    private String name;
    private String email;
    private String password;

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User(String token, String name, String email) {
        this.token = token;
        this.name = name;
        this.email = email;
    }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    protected User(Parcel in) {
        token = in.readString();
        name = in.readString();
        email = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(token);
        parcel.writeString(name);
        parcel.writeString(email);
    }
}
