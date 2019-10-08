package com.cuonghx.teacher.teachercheckin.screens.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.cuonghx.teacher.teachercheckin.CheckInApplication;
import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.source.remote.AuthenticationRemoteDataSource;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.BaseResponse;
import com.cuonghx.teacher.teachercheckin.screens.BaseActivity;

import com.cuonghx.teacher.teachercheckin.screens.login.LoginActivity;
import com.cuonghx.teacher.teachercheckin.service.GPSTracker;
import com.cuonghx.teacher.teachercheckin.util.StringUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class CreateActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, TextWatcher ,  GoogleApiClient.ConnectionCallbacks, LocationListener {

    protected GeoDataClient mGeoDataClient;
    private Place place;
    private LocationGPS locationGPS;
    private TextInputEditText mTextInputEditTextName;

    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private TextInputEditText mInputEditTextLocation;
    private Dialog dialog;
    private Thread t;
//    private GPSTracker gps;


    private LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();



    @Override
    protected int getLayoutResource() {
        return R.layout.activity_create;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setupView();
    }
    private void setupView(){
        setTitle("Tạo khoá học mới");
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);
        mTextInputEditTextName = findViewById(R.id.tiet_name_course);
        mInputEditTextLocation = findViewById(R.id.tiet_location);
        findViewById(R.id.iv_my_location).setOnClickListener(this);
        androidx.appcompat.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#32a5d8")));
        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        actionBarToolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.WHITE);

//        mInputEditTextLocation.addTextChangedListener(this);
        mInputEditTextLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
//                    Toast.makeText(getApplicationContext(), "Got the focus", Toast.LENGTH_LONG).show();
                    try {
                        AutocompleteFilter filter = new AutocompleteFilter.Builder().setCountry("VN").build();
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setFilter(filter).build(CreateActivity.this);
                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException e) {
                        // TODO: Handle the error.
                        Log.d("cuonghx", "onClick: " + e.getLocalizedMessage());
                    } catch (GooglePlayServicesNotAvailableException e) {
                        // TODO: Handle the error.
                        Log.d("cuonghx", "onClick: " + e.getLocalizedMessage());
                    }
                }
            }
        });

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        findViewById(R.id.bt_create).setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            Log.e("Google API", "Connecting");
            mGoogleApiClient.connect();
        }
        Log.d("cuonghx", "onResume: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            mInputEditTextLocation.clearFocus();
            if (resultCode == RESULT_OK) {
                locationGPS = null;
                Place place = PlaceAutocomplete.getPlace(this, data);
                this.place = place;
                Log.i("cuonghx", "Place: " + place.getName());
                mInputEditTextLocation.setText(place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("cuonghx", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.d("cuongx", "onActivityResult: cancel");
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("fail", "onConnectionFailed: ");
    }

    private void reqestLocationTurnOn(){
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
//        }
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_gps_setting);
            dialog.findViewById(R.id.bt_cancel_dialog).setOnClickListener(this);
            dialog.findViewById(R.id.bt_setting_dialog).setOnClickListener(this);
            dialog.show();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_my_location:
                place = null;
                final GPSTracker gps = new GPSTracker(this);
                if(gps.canGetLocation()){
                    if (locationGPS != null && locationGPS.lattitude == 0 && locationGPS.longtitude == 0){
                        mInputEditTextLocation.setText("Xin chờ để lấy vị tri");
                    }else if (locationGPS != null) {
                        final String strLocation = StringUtils.convert(locationGPS.lattitude, locationGPS.longtitude);
                        mInputEditTextLocation.setText(strLocation);
                    }
                }else{
                    reqestLocationTurnOn();
                }
                break;
            case R.id.bt_create:
                String name = String.valueOf(mTextInputEditTextName.getText());
                if (StringUtils.checkNullOrEmpty(name)){
                    Toast.makeText(this, getText(R.string.msg_name_should_not_empty), Toast.LENGTH_SHORT).show();
                }
                else if (place == null && locationGPS == null) {
                    Toast.makeText(this, getText(R.string.ms_select_place), Toast.LENGTH_SHORT).show();
                }else if (place != null){
                    mCompositeDisposable.add(
                        AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                                .createCourse(CheckInApplication.getInstance().getCurrentEmail(), name, place.getLatLng().latitude, place.getLatLng().longitude, String.valueOf(place.getName()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe(new Consumer<Disposable>() {
                                    @Override
                                    public void accept(Disposable disposable) throws Exception {

                                    }
                                }).subscribe(new Consumer<BaseResponse>() {
                            @Override
                            public void accept(BaseResponse baseResponse) throws Exception {
                                Toast.makeText(CreateActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                CreateActivity.super.onBackPressed();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                handleErrors(throwable);
                            }
                        })
                    );
                }else if (locationGPS != null) {
                    mCompositeDisposable.add(
                        AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                                .createCourse(CheckInApplication.getInstance().getCurrentEmail(), name, locationGPS.lattitude, locationGPS.longtitude, "")
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe(new Consumer<Disposable>() {
                                    @Override
                                    public void accept(Disposable disposable) throws Exception {

                                    }
                                }).subscribe(new Consumer<BaseResponse>() {
                            @Override
                            public void accept(BaseResponse baseResponse) throws Exception {
                                Toast.makeText(CreateActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                CreateActivity.super.onBackPressed();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                handleErrors(throwable);
                            }
                        })
                    );
                }
                break;
            case R.id.bt_cancel_dialog:{
                dialog.cancel();
                break;
            }
            case R.id.bt_setting_dialog:
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                locationGPS = new LocationGPS(0,0);
                break;
        }
    }
    private void handleErrors(Throwable throwable) {
        if (throwable instanceof HttpException) {
            handleHttpExceptions((HttpException) throwable);
            return;
        } else if (throwable instanceof UnknownHostException) {
            Toast.makeText(this, R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this,
                R.string.msg_something_went_wrong,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void handleHttpExceptions(HttpException httpException) {
        switch (httpException.code()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Toast.makeText(this,
                        R.string.msg_wrong_email_or_password,
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, httpException.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.d("cuonghx", "beforeTextChanged: 1");
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.d("cuonghx", "beforeTextChanged: 2");
    }

    @Override
    public void afterTextChanged(Editable editable) {
        Log.d("cuonghx", "beforeTextChanged: 3");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            Log.d("cuonghx", "onLocationChanged: 456" );
            this.locationGPS = new LocationGPS(location.getLatitude(),location.getLongitude());
//            mInputEditTextLocation.setText(StringUtils.convert(locationGPS.lattitude, locationGPS.longtitude));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        locationGPS = new LocationGPS(location.getLatitude(), location.getLongitude());
        Log.d("cuonghx", "onLocationChanged: 123" );
        mInputEditTextLocation.setText(StringUtils.convert(locationGPS.lattitude, locationGPS.longtitude));
    }

    private class LocationGPS{
        public double lattitude;
        public double longtitude;

        public LocationGPS(double latitude, double longtitude){
            this.lattitude = latitude;
            this.longtitude = longtitude;
        }
    }
}
