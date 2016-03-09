package com.example.yinnan.sunshine.UI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yinnan.sunshine.Models.Forecast;
import com.example.yinnan.sunshine.Models.CurrentWeather;
import com.example.yinnan.sunshine.Models.DailyWeather;
import com.example.yinnan.sunshine.Models.HourlyWeather;
import com.example.yinnan.sunshine.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public final static String TAG = MainActivity.class.getSimpleName();
    public final static String HOUR_TAG = "hourly";
    public final static String DAY_TAG = "daily";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public final static int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Forecast mForecast;
    private double mLatitude;// = 37.8267;
    private double mLongitude;// = -122.423;

    @Bind(R.id.temperatureLabel) TextView mTemperatureLabel;
    @Bind(R.id.timeLabel) TextView mTimeLabel;
    @Bind(R.id.iconImageView) ImageView mIconImageView;
    @Bind(R.id.humidityLabel) TextView mHumidityLabel;
    @Bind(R.id.rainLabel) TextView mRainLabel;
    @Bind(R.id.summaryLabel) TextView mSummaryLabel;
    @Bind(R.id.refreshImageView) ImageView mRefreshImageView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.dailyButton) Button mDailyButton;
    @Bind(R.id.hourlyButton) Button mHourlyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get location
                getJSONData(mLatitude, mLongitude);
            }
        });

        mDailyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DailyActivity.class);
                intent.putExtra(DAY_TAG, mForecast.getDailyWeathers());
                startActivity(intent);
            }
        });

        mHourlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HourlyActivity.class);
                intent.putExtra(HOUR_TAG, mForecast.getHourlyWeathers());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
        }
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        getLocation();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(MainActivity.this,
                        "Please allow the app to access your location",
                        Toast.LENGTH_LONG).show();
                onPause();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
            }
        }
        else {
            Log.d(TAG, "Permission granted!");
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                Log.d(TAG, "No location can be returned on getLastLocation()");
                LocationServices.FusedLocationApi.requestLocationUpdates
                        (mGoogleApiClient, mLocationRequest, this);
            } else {
                toggleRefresh();
                handleNewLocation(location);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        toggleRefresh();
        Log.d(TAG, "new location got" + location.toString());
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, "Current Location is " + location.toString());
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        toggleRefresh();
        getJSONData(mLatitude, mLongitude);
    }

    private void getJSONData(double latitude, double longitude) {
        toggleRefresh();
        //set url
        String apiKey = "00e4ad0345969fa8b1a81de0646517a5";
        String url = "https://api.forecast.io/forecast/"
                + apiKey + "/" + latitude + "," + longitude;

        //create http client to get the data.
        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    Log.e(TAG, "call failed");
                    Toast.makeText(MainActivity.this, "Call failed",
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            //create Forecast object to hold all weather objects;
                            mForecast = getForecast(response.body().string());
                            //update ui from ui thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toggleRefresh();
                                    updateUI(mForecast);
                                }
                            });
                        } catch (JSONException e) {
                            Log.e(TAG, "" + e);
                        }
                    } else {
                        Log.e(TAG, "response failed");
                    }
                }
            });
        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toggleRefresh();
                }
            });
            Log.e(TAG, "No internet connections.");
            Toast.makeText(MainActivity.this, "No internet connections.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateUI(Forecast forecast) {
        CurrentWeather currentWeather = forecast.getCurrentWeather();
        mTemperatureLabel.setText(currentWeather.getTemperature()+"");
        mTimeLabel.setText("At "+ currentWeather.getFormattedTime() + " it will be");
        mHumidityLabel.setText(currentWeather.getHumidity()+"");
        mRainLabel.setText(currentWeather.getRain() + "%");
        mSummaryLabel.setText(currentWeather.getSummary());
        mIconImageView.setImageResource(currentWeather.getIconId());
        Toast.makeText(MainActivity.this, mLatitude+" "+mLongitude, Toast.LENGTH_LONG).show();
    }

    private Forecast getForecast(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();
        forecast.setCurrentWeather(getCurrentWeather(jsonData));
        forecast.setHourlyWeathers(getHourlyWeather(jsonData));
        forecast.setDailyWeathers(getDailyWeather(jsonData));
        return forecast;
    }

    private DailyWeather[] getDailyWeather(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONArray daysJSONArray = forecast.getJSONObject("daily").getJSONArray("data");
        DailyWeather[] days = new DailyWeather[daysJSONArray.length()];
        for (int i=0;i<daysJSONArray.length();i++) {
            JSONObject day = daysJSONArray.getJSONObject(i);
            days[i] = new DailyWeather();
            days[i].setTimeZone(timezone);
            days[i].setTime(day.getLong("time"));
            days[i].setSummary(day.getString("summary"));
            days[i].setIcon(day.getString("icon"));
            days[i].setTemperature(day.getDouble("temperatureMax"));
        }
        return days;
    }

    private HourlyWeather[] getHourlyWeather(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONArray hoursJSONArray = forecast.getJSONObject("hourly").getJSONArray("data");
        HourlyWeather[] hours = new HourlyWeather[hoursJSONArray.length()];
        for (int i=0;i<hoursJSONArray.length();i++) {
            JSONObject hour = hoursJSONArray.getJSONObject(i);
            hours[i] = new HourlyWeather();
            hours[i].setTime(hour.getLong("time"));
            hours[i].setSummary(hour.getString("summary"));
            hours[i].setIcon(hour.getString("icon"));
            hours[i].setTemperature(hour.getDouble("temperature"));
            hours[i].setTimezone(timezone);
        }
        return hours;
    }

    private CurrentWeather getCurrentWeather(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        CurrentWeather current = new CurrentWeather();
        current.setTimeZone(forecast.getString("timezone"));
        JSONObject currentlyData = forecast.getJSONObject("currently");
        current.setTime(currentlyData.getLong("time"));
        current.setSummary(currentlyData.getString("summary"));
        current.setIcon(currentlyData.getString("icon"));
        current.setTemperature(currentlyData.getDouble("temperature"));
        current.setHumidity(currentlyData.getDouble("humidity"));
        current.setRain(currentlyData.getDouble("precipProbability"));
        return current;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " +
                    connectionResult.getErrorCode());
        }
    }
}
