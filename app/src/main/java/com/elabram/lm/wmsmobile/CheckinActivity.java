package com.elabram.lm.wmsmobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elabram.lm.wmsmobile.model.Office;
import com.elabram.lm.wmsmobile.rest.ApiClient;
import com.elabram.lm.wmsmobile.utilities.AppInfo;
import com.elabram.lm.wmsmobile.utilities.GPSTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.elabram.lm.wmsmobile.utilities.AppInfo.isOnline;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.token;

public class CheckinActivity extends AppCompatActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.buttonCheckin)
    Button buttonCheckin;

    @BindView(R.id.rootView)
    LinearLayout rootView;

    @BindView(R.id.fabMyLocation)
    FloatingActionButton fabMyLocation;

    @BindView(R.id.dataLayout)
    LinearLayout dataLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.date)
    TextView tvDate;

    @BindView(R.id.time_start)
    TextView tvStart;

    @BindView(R.id.time_end)
    TextView tvEnd;

    @BindView(R.id.tvFirstLocation)
    TextView tvFirstLocation;

    @BindView(R.id.tvLastLocation)
    TextView tvLastLocation;

    @BindView(R.id.relativeEnabled)
    RelativeLayout rel_online;

    @BindView(R.id.relativeDisabled)
    RelativeLayout rel_offline;

    @BindView(R.id.buttonRefresh)
    ImageView buttonRefresh;

    @BindDrawable(R.drawable.bg_confirm_white)
    Drawable bg_confirm;

    private String TAG = CheckinActivity.class.getSimpleName();

    // Map Variable
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private GoogleMap map;
    private double myLat;
    private double myLong;
    private LatLng myLatLong;
    private SupportMapFragment mapFragment;

    private String task_id;
    private String location_name;
    private String lat_, long_;
    private ArrayList<Office> offices;
    float[] resultApi = new float[1];
    private String site_name;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        offices = new ArrayList<>();

        progressDialog = new ProgressDialog(this, R.style.AppThemeLoading);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait...");

        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getSharedUserDetail();
        getSharedTaskId();
        buildGoogleApiClient();
        buttonCheckin.setVisibility(View.GONE);

        cekInternet();
        buttonRefresh.setOnClickListener(view -> cekInternet());

        GPSTracker gpsTracker = new GPSTracker(this);
        myLat = gpsTracker.getLatitude();
        myLong = gpsTracker.getLongitude();
        myLatLong = new LatLng(myLat, myLong);

        fabMyLocation.setOnClickListener(view -> myLocation());

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                locationChanged(locationResult);
            }
        };
    }

    private void cekInternet() {
        if (AppInfo.isOnline(this)) {
            rel_online.setVisibility(View.VISIBLE);
            showProgress();
            retrofitSite();
            retrofitCheckinStatus();
            rel_offline.setVisibility(View.GONE);
        } else {
            rel_online.setVisibility(View.GONE);
            rel_offline.setVisibility(View.VISIBLE);
        }
    }

    private void getSharedTaskId() {
        SharedPreferences preferences = getSharedPreferences("listclick", 0);
        task_id = preferences.getString("task_id", "");
        location_name = preferences.getString("location_name", "");
        //date_ = preferences.getString("date_", "");
        lat_ = preferences.getString("lat_", "");
        long_ = preferences.getString("long_", "");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    public void requestLocationUpdates() {
        // 120000 = 2 minute
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); // two minute interval
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    private void locationChanged(LocationResult locationResult) {
        SharedPreferences preferences = getSharedPreferences("json_status", 0);
        //noinspection unused
        String status_checkin = preferences.getString("status", "");
        double distance_limit = 250.00;

        for (@SuppressWarnings("unused") Location location : locationResult.getLocations()) {

            // Mock Checking
            boolean isMock = location.isFromMockProvider();
            //Log.e(TAG, "locationChanged: isMock " + isMock);

            for (int k = 0; k < offices.size(); k++) {
                Office office = offices.get(k);
                Double d_lat = Double.valueOf(office.getOc_lat());
                Double d_long = Double.valueOf(office.getOc_long());

                // Find the distance
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), d_lat, d_long, resultApi);

                // Convert to String & Double
                String s_distanceToOffice = String.valueOf(resultApi[0]);
                Double d_distanceToOffice = Double.parseDouble(s_distanceToOffice);
                Log.e(TAG, "locationChanged: Distance " + s_distanceToOffice);

                if (d_distanceToOffice < distance_limit) {
                    site_name = office.getOc_site();
                    buttonCheckin.setVisibility(View.VISIBLE);
                    if (isMock) {
                        processTheMock();
                    } else {
                        processNoMock();
                    }
                    break;
                } else {
                    buttonCheckin.setVisibility(View.GONE);
                }
            }

        }
    }

    private void processNoMock() {
        buttonCheckin.setOnClickListener(view -> {
            Log.e(TAG, "locationChanged: Click Checkin ");
            if (isOnline(this)) {
                retrofitCheckin();
            }
            else {
                Snackbar snackbar = Snackbar.make(rootView, "Please check your internet connection", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void processTheMock() {
        Log.e(TAG, "locationChanged: " + "Yes Mock");
        Snackbar snackbar = Snackbar.make(rootView,
                "We detected the use of fake GPS application. As logo_indosat consequence, " +
                        "we will report this illegal action to HR to be processed accordingly.",
                Snackbar.LENGTH_LONG
        );
        snackbar.show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    private void myLocation() {
        mapFragment.getMapAsync(googleMap ->
        {
            map = googleMap;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    map.moveCamera(CameraUpdateFactory.newLatLng(myLatLong));
                    map.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLng(myLatLong));
                map.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        });
    }

    private void showProgress() {
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    private void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void retrofitCheckinStatus() {
        Call<ResponseBody> call = new ApiClient().getApiService().loadStatusCheckin(token);
        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                dismissProgress();
                try {
                    //noinspection ConstantConditions
                    String contentResponse = new String(response.body().bytes());
                    Log.e(TAG, "onResponse: Checkin Status " + contentResponse);
                    JSONObject jsonObject = new JSONObject(contentResponse);
                    String response_code = jsonObject.getString("response_code");
                    switch (response_code) {
                        case "401":
                            String message = jsonObject.getString("message");
                            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
                            snackbar.show();
                            break;
                        case "200":
                            JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                            String realtime_date = jsonObject1.getString("is_date");
                            String time_first = jsonObject1.getString("time_first");
                            String location_first = jsonObject1.getString("location_first");

                            String time_last = jsonObject1.getString("time_last");
                            String location_last = jsonObject1.getString("location_last");

                            tvDate.setText(realtime_date);

                            String replaceFirst = time_first.replace(".", ":");
                            String replaceLast = time_last.replace(".", ":");

                            if (!time_first.isEmpty())
                                tvStart.setText(replaceFirst);
                            else
                                tvStart.setText("-");

                            if (!time_last.isEmpty())
                                tvEnd.setText(replaceLast);
                            else
                                tvEnd.setText("-");

                            if (!location_first.isEmpty())
                                tvFirstLocation.setText("(" + location_first + ")");
                            //else
                            //    tvFirstLocation.setText("(-)");

                            if (!location_last.isEmpty())
                                tvLastLocation.setText("(" + location_last + ")");
                            //else
                            //    tvLastLocation.setText("(-)");

                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: CheckinStatus " + t.getCause());
                Toast.makeText(CheckinActivity.this, "Please check your internet & try again", Toast.LENGTH_SHORT).show();
                dismissProgress();
            }
        });
    }

    private void retrofitCheckin() {
        showProgress();
        Call<ResponseBody> call = new ApiClient().getApiService().checkin(token, site_name);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                retrofitCheckinStatus();
                try {
                    //noinspection ConstantConditions
                    String contentResponse = new String(response.body().bytes());
                    Log.e(TAG, "onResponse: Checkin " + contentResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                dismissProgress();
                Toast.makeText(CheckinActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: Checkin " + t.getCause());
            }
        });
    }

    private void retrofitSite() {
        Call<ResponseBody> call = new ApiClient().getApiService().siteList(token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    //noinspection ConstantConditions
                    String content = new String(response.body().bytes());
                    Log.e(TAG, "onResponse: Retrofit Site List " + content);
                    JSONObject jsonObject = new JSONObject(content);

                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String lat = jsonObject1.getString("lat");
                        String lng = jsonObject1.getString("long");
                        //noinspection unused
                        String city = jsonObject1.getString("city");
                        String site_name = jsonObject1.getString("site_name");

                        Office office = new Office();
                        office.setOc_lat(lat);
                        office.setOc_long(lng);
                        office.setOc_site(site_name);
                        offices.add(office);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Site " + t.getCause());
            }
        });
    }

    private void getSharedUserDetail() {
        SharedPreferences preferences = getSharedPreferences(AppInfo.PREFS_LOGIN, Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //this.onCreate(null);
        if (mGoogleApiClient != null && mFusedLocationClient != null) {
            requestLocationUpdates();
        } else {
            buildGoogleApiClient();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        /* Permission */
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        /* Default Map Setting */
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        /* First Camera Position */
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(myLat, myLong))
                .zoom(12) // default 15
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        if (!lat_.isEmpty() && !long_.isEmpty()) {
            final Double dlat = Double.parseDouble(lat_);
            final Double dlng = Double.parseDouble(long_);
            LatLng latLngTask = new LatLng(dlat, dlng);

            map.addMarker(new MarkerOptions()
                    .position(latLngTask)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .title("Task ID: " + task_id)
                    .snippet("Description: " + location_name));

            int stroke_c = 0xffff0000;
            int transp = 0x44ff0000;
            map.addCircle(new CircleOptions()
                    .center(latLngTask)
                    .radius(100)
                    .fillColor(transp)
                    .strokeWidth(2)
                    .strokeColor(stroke_c)
            );
        }
    }

}
