package com.example.yinnan.sunshine.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.yinnan.sunshine.Models.DailyWeather;
import com.example.yinnan.sunshine.R;

public class DailyAdapter extends BaseAdapter {
    Context mContext;
    DailyWeather[] mDays;

    public DailyAdapter(Context context, DailyWeather[] days) {
        mContext = context;
        mDays = days;
    }

    @Override
    public int getCount() {
        return mDays.length;
    }

    @Override
    public Object getItem(int position) {
        return mDays[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        //if convertView doesn't exist, inflate from daily_list_item layout
        //initialize all fields in view
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item,
                    null);
            view = new ViewHolder();
            view.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            view.dateLabel = (TextView) convertView.findViewById(R.id.dateLabel);
            view.temperatureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            convertView.setTag(view);
        }
        else {
            view = (ViewHolder) convertView.getTag();
        }

        //update each item in the list with DailyWeather object
        DailyWeather day = mDays[position];
        view.temperatureLabel.setText(day.getTemperature()+"");
        view.iconImageView.setImageResource(day.getIconId());
        if (position == 0) {
            view.dateLabel.setText("Today "+day.getDate());
        }
        else {
            view.dateLabel.setText(day.getDay() + " " + day.getDate());
        }
        return convertView;
    }

    public static class ViewHolder {
        TextView temperatureLabel;
        TextView dateLabel;
        ImageView iconImageView;
    }
}
