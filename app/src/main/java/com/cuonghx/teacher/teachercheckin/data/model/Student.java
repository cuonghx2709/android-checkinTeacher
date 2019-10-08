package com.cuonghx.teacher.teachercheckin.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Student implements Parcelable {
    @Expose
    @SerializedName("student_id")
    private int mId;

    @Expose
    @SerializedName("student_name")
    private String mName;
    @Expose
    @SerializedName("check_in")
    private int mCount;

    public static final Parcelable.Creator<Course> CREATOR = new Parcelable.Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeInt(mCount);
    }

    protected Student(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mCount = in.readInt();
    }

    public Student(int id, String name, int count) {
        mId = id;
        mName = name;
        mCount = count;
    }

    public String getmName() {
        return mName;
    }

    public int getmCount() {
        return mCount;
    }

    public int getmId() {
        return mId;
    }
}
