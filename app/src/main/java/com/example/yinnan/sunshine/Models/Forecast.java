package com.example.yinnan.sunshine.Models;

import com.example.yinnan.sunshine.R;

public class Forecast {
    private CurrentWeather mCurrentWeather;
    private HourlyWeather[] mHourlyWeathers;
    private DailyWeather[] mDailyWeathers;

    public CurrentWeather getCurrentWeather() {
        return mCurrentWeather;
    }

    public void setCurrentWeather(CurrentWeather CurrentWeather) {
        mCurrentWeather = CurrentWeather;
    }

    public HourlyWeather[] getHourlyWeathers() {
        return mHourlyWeathers;
    }

    public void setHourlyWeathers(HourlyWeather[] HourlyWeathers) {
        mHourlyWeathers = HourlyWeathers;
    }

    public DailyWeather[] getDailyWeathers() {
        return mDailyWeathers;
    }

    public void setDailyWeathers(DailyWeather[] dailyWeathers) {
        mDailyWeathers = dailyWeathers;
    }

    public static int getIconId(String icon) {
        if (icon.equals("clear-day")) return R.drawable.sunny;
        else if (icon.equals("clear-night")) return R.drawable.clear_night;
        else if (icon.equals("partly-cloudy-day")) return R.drawable.partly_cloudy;
        else if (icon.equals("partly-cloudy-night")) return R.drawable.cloudy_night;
        else if (icon.equals("rain")) return R.drawable.rain;
        else if (icon.equals("snow")) return R.drawable.snow;
        else if (icon.equals("sleet")) return R.drawable.sleet;
        else if (icon.equals("wind")) return R.drawable.wind;
        else if (icon.equals("fog")) return R.drawable.fog;
        else return R.drawable.cloudy;
    }
}
