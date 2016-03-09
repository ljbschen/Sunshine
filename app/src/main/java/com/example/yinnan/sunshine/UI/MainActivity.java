package com.example.yinnan.sunshine.UI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

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
    private static HashMap<String, String> mStateMap;
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
    @Bind(R.id.locationLabel) TextView mLocationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        populateStates();

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
        mLocationLabel.setText(currentWeather.getLocation());
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
        String location = getCity();
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
            days[i].setLocation(location);
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
        String location = getCity();
        current.setTimeZone(forecast.getString("timezone"));
        JSONObject currentlyData = forecast.getJSONObject("currently");
        current.setTime(currentlyData.getLong("time"));
        current.setSummary(currentlyData.getString("summary"));
        current.setIcon(currentlyData.getString("icon"));
        current.setTemperature(currentlyData.getDouble("temperature"));
        current.setHumidity(currentlyData.getDouble("humidity"));
        current.setRain(currentlyData.getDouble("precipProbability"));
        current.setLocation(location);
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

    public String getCity() {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            if (geocoder.getFromLocation(mLatitude, mLongitude, 1).size()>0) {
                Address address = geocoder.getFromLocation(mLatitude, mLongitude, 1).get(0);
                String state = address.getAdminArea();
                if (mStateMap.containsKey(address.getAdminArea())) {
                    state = mStateMap.get(address.getAdminArea());
                }
                return address.getLocality() + ", " + state;
            }
            else {
                return "";
            }
        }catch (IOException e) {
            e.toString();
            return "";
        }
    }

    private static void populateStates() {
        if (mStateMap == null) {
            mStateMap = new HashMap<String, String>();
            mStateMap.put("Alabama", "AL");
            mStateMap.put("Alaska", "AK");
            mStateMap.put("Alberta", "AB");
            mStateMap.put("American Samoa", "AS");
            mStateMap.put("Arizona", "AZ");
            mStateMap.put("Arkansas", "AR");
            mStateMap.put("Armed Forces (AE)", "AE");
            mStateMap.put("Armed Forces Americas", "AA");
            mStateMap.put("Armed Forces Pacific", "AP");
            mStateMap.put("British Columbia", "BC");
            mStateMap.put("California", "CA");
            mStateMap.put("Colorado", "CO");
            mStateMap.put("Connecticut", "CT");
            mStateMap.put("Delaware", "DE");
            mStateMap.put("District Of Columbia", "DC");
            mStateMap.put("Florida", "FL");
            mStateMap.put("Georgia", "GA");
            mStateMap.put("Guam", "GU");
            mStateMap.put("Hawaii", "HI");
            mStateMap.put("Idaho", "ID");
            mStateMap.put("Illinois", "IL");
            mStateMap.put("Indiana", "IN");
            mStateMap.put("Iowa", "IA");
            mStateMap.put("Kansas", "KS");
            mStateMap.put("Kentucky", "KY");
            mStateMap.put("Louisiana", "LA");
            mStateMap.put("Maine", "ME");
            mStateMap.put("Manitoba", "MB");
            mStateMap.put("Maryland", "MD");
            mStateMap.put("Massachusetts", "MA");
            mStateMap.put("Michigan", "MI");
            mStateMap.put("Minnesota", "MN");
            mStateMap.put("Mississippi", "MS");
            mStateMap.put("Missouri", "MO");
            mStateMap.put("Montana", "MT");
            mStateMap.put("Nebraska", "NE");
            mStateMap.put("Nevada", "NV");
            mStateMap.put("New Brunswick", "NB");
            mStateMap.put("New Hampshire", "NH");
            mStateMap.put("New Jersey", "NJ");
            mStateMap.put("New Mexico", "NM");
            mStateMap.put("New York", "NY");
            mStateMap.put("Newfoundland", "NF");
            mStateMap.put("North Carolina", "NC");
            mStateMap.put("North Dakota", "ND");
            mStateMap.put("Northwest Territories", "NT");
            mStateMap.put("Nova Scotia", "NS");
            mStateMap.put("Nunavut", "NU");
            mStateMap.put("Ohio", "OH");
            mStateMap.put("Oklahoma", "OK");
            mStateMap.put("Ontario", "ON");
            mStateMap.put("Oregon", "OR");
            mStateMap.put("Pennsylvania", "PA");
            mStateMap.put("Prince Edward Island", "PE");
            mStateMap.put("Puerto Rico", "PR");
            mStateMap.put("Quebec", "PQ");
            mStateMap.put("Rhode Island", "RI");
            mStateMap.put("Saskatchewan", "SK");
            mStateMap.put("South Carolina", "SC");
            mStateMap.put("South Dakota", "SD");
            mStateMap.put("Tennessee", "TN");
            mStateMap.put("Texas", "TX");
            mStateMap.put("Utah", "UT");
            mStateMap.put("Vermont", "VT");
            mStateMap.put("Virgin Islands", "VI");
            mStateMap.put("Virginia", "VA");
            mStateMap.put("Washington", "WA");
            mStateMap.put("West Virginia", "WV");
            mStateMap.put("Wisconsin", "WI");
            mStateMap.put("Wyoming", "WY");
            mStateMap.put("Yukon Territory", "YT");
        }
    }
}
