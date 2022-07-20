package com.example.preface.autonavi;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchListParcel implements Parcelable {
    public int age;
    public int count;
    public String name;
    public SearchListParcel(){

    }
    protected SearchListParcel(Parcel in) {
        age = in.readInt();
        name = in.readString();
        count = in.readInt();


    }

    public static final Creator<SearchListParcel> CREATOR = new Creator<SearchListParcel>() {
        @Override
        public SearchListParcel createFromParcel(Parcel in) {
            return new SearchListParcel(in);
        }

        @Override
        public SearchListParcel[] newArray(int size) {
            return new SearchListParcel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(age);
        dest.writeString(name);
        dest.writeInt(count);
    }


    @Override
    public String toString() {
        return "TestParcel{" +
                "age=" + age +
                ", count=" + count +
                ", name='" + name + '\'' +
                '}';
    }
}

