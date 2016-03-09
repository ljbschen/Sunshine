package com.example.yinnan.sunshine.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class HourlyWeather implements Parcelable {
    private long mTime;
    private String mSummary;
    private String mIcon;
    private double mTemperature;
    private String mTimezone;

    public HourlyWeather() {}

    protected HourlyWeather(Parcel in) {
        mTime = in.readLong();
        mSummary = in.readString();
        mIcon = in.readString();
        mTemperature = in.readDouble();
        mTimezone = in.readString();
    }

    public static final Creator<HourlyWeather> CREATOR = new Creator<HourlyWeather>() {
        @Override
        public HourlyWeather createFromParcel(Parcel in) {
            return new HourlyWeather(in);
        }

        @Override
        public HourlyWeather[] newArray(int size) {
            return new HourlyWeather[size];
        }
    };

    public long getTime() {
        return mTime;
    }

    public String getHour() {
        SimpleDateFormat formatter = new SimpleDateFormat("hh a");
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date date = new Date(mTime*1000);
        return formatter.format(date);
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public int getIconId() {
        return Forecast.getIconId(mIcon);
    }

    public int getTemperature() {
        return (int)Math.round(mTemperature);
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(String timezone) {
        mTimezone = timezone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTime);
        dest.writeString(mSummary);
        dest.writeString(mIcon);
        dest.writeDouble(mTemperature);
        dest.writeString(mTimezone);
    }
}
