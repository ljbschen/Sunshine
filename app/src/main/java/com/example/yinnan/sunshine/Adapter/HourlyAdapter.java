package com.example.yinnan.sunshine.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yinnan.sunshine.Models.HourlyWeather;
import com.example.yinnan.sunshine.R;

//adapter of HourViewHolders
public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.HourViewHolder> {

    HourlyWeather[] mHours;
    Context mContext;

    public HourlyAdapter(Context context, HourlyWeather[] hours) {
        mContext = context;
        mHours = hours;
    }

    @Override
    //create each item holder
    public HourViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hourly_list_item, parent, false);
        return new HourViewHolder(view);
    }

    @Override
    //bind fields to the view.
    public void onBindViewHolder(HourViewHolder holder, int position) {
        holder.bindHour(mHours[position]);
    }

    @Override
    public int getItemCount() {
        return mHours.length;
    }

    //view holder for each item. using layout hourly_list_item.xml
    public class HourViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        private TextView mTemperatureLabel, mSummary, mTime;
        private ImageView mIconImageView;

        public HourViewHolder(View itemView) {
            super(itemView);
            //initialize all fields, get references to all components
            //used to be done by inflater
            mTemperatureLabel = (TextView) itemView.findViewById(R.id.temperatureLabel);
            mSummary = (TextView) itemView.findViewById(R.id.summaryLabel);
            mTime = (TextView) itemView.findViewById(R.id.timeLabel);
            mIconImageView = (ImageView) itemView.findViewById(R.id.iconImageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String time = mTime.getText().toString();
            String summary = mSummary.getText().toString();
            String temp = mTemperatureLabel.getText().toString();
            String msg = String.format("At %s it will be %s degree and %s",
                    time, temp, summary);
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        }

        public void bindHour(HourlyWeather hour) {
            //update ui
            mTemperatureLabel.setText(hour.getTemperature()+"");
            mSummary.setText(hour.getSummary());
            mIconImageView.setImageResource(hour.getIconId());
            mTime.setText(hour.getHour());
        }
    }


}
