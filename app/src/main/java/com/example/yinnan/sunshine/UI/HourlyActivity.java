package com.example.yinnan.sunshine.UI;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.yinnan.sunshine.Adapter.HourlyAdapter;
import com.example.yinnan.sunshine.Models.HourlyWeather;
import com.example.yinnan.sunshine.R;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HourlyActivity extends AppCompatActivity {
    HourlyWeather[] mHours;

    @Bind(R.id.recycleView) RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.HOUR_TAG);
        mHours = Arrays.copyOf(parcelables, parcelables.length, HourlyWeather[].class);

        mRecyclerView.setAdapter(new HourlyAdapter(this, mHours));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
    }
}
