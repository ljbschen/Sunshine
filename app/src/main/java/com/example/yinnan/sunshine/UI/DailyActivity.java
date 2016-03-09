package com.example.yinnan.sunshine.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yinnan.sunshine.Adapter.DailyAdapter;
import com.example.yinnan.sunshine.Models.DailyWeather;
import com.example.yinnan.sunshine.R;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DailyActivity extends Activity {

    DailyWeather[] mDays;
    @Bind(android.R.id.list) ListView mListView;
    @Bind(android.R.id.empty) TextView mEmptyTextView;
    @Bind(R.id.locationLabel) TextView mLocationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);
        ButterKnife.bind(this);
        //get day[] data
        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAY_TAG);
        mDays = Arrays.copyOf(parcelables, parcelables.length, DailyWeather[].class);

        mLocationLabel.setText(mDays[0].getLocation());
        //set list adapter
        mListView.setAdapter(new DailyAdapter(this, mDays));
        mListView.setEmptyView(mEmptyTextView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String date = mDays[position].getDate();
                String summary = mDays[position].getSummary();
                String temp = mDays[position].getTemperature()+"";
                String msg = String.format("On %s it will be %s degree and %s",
                        date, temp, summary);
                Toast.makeText(parent.getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

    }
}
