package com.example.sudo.whereisivan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 50;
    TextView tvLokacija;
    GoogleMap mGoogleMap;
    MapFragment mMapFragment;
    LocationListener mLocationListener;
    LocationManager mLocationManager;
    private GoogleMap.OnMapClickListener mCustomMapClickListener;
    String imeLokacije;
    ImageButton ibSlikaj;
    Intent kamera;
    Uri slikaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initialize();

        this.mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.mLocationListener = new SimpleLocationListener();
    }


    private void initialize() {
        this.mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        this.mMapFragment.getMapAsync(this);
        this.mCustomMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions newMarkerOptions = new MarkerOptions();
                newMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                newMarkerOptions.title("My place");
                newMarkerOptions.snippet("I declare this my teritory");
                newMarkerOptions.position(latLng);
                mGoogleMap.addMarker(newMarkerOptions);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(hasLocationPermision() == false){
            requestPermission();
        }
    }

    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(MainActivity.this,
                permissions, REQUEST_LOCATION_PERMISSION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this.hasLocationPermision()){
            startTracking();
        }
    }

    private boolean hasLocationPermision() {
        String LocationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        int status = ContextCompat.checkSelfPermission(this, LocationPermission);
        if (status == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopTracking();
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        UiSettings uiSettings = this.mGoogleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        this.mGoogleMap.setOnMapClickListener(this.mCustomMapClickListener);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;

        }
        this.mGoogleMap.setMyLocationEnabled(true);
    }

    private void startTracking() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String locationProvider = this.mLocationManager.getBestProvider(criteria, true);
        long minTime = 10000;
        float minDistance = 100;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.mLocationManager.requestLocationUpdates(locationProvider, minTime, minDistance,
                this.mLocationListener);
    }

    private void stopTracking() {
        Log.d("Praćenje", "Stop s praćenjem");
        this.mLocationManager.removeUpdates(this.mLocationListener);
    }

    @Override
    public void onClick(View v) {
        kamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), imeLokacije + ".jpg");
        kamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        slikaUri = Uri.fromFile(photo);

        if (canBeCalled(kamera)) {
            startActivityForResult(kamera, 1);
        } else {
            Log.e("TAG", "Nema kamere");
        }

    }

    private boolean canBeCalled(Intent kamera) {
        PackageManager manager = this.getPackageManager();
        if(kamera.resolveActivity(manager) != null){
            return true;
        }
        return false;
    }

    private class SimpleLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) { updateLocationText(location);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private void updateLocationText(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocation)
                .build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,5));
        String message =
                "Lat: " + location.getLatitude() + "\nLon:" + location.getLongitude() + "\n";
        tvLokacija.setText(message);
        MarkerOptions newMarkerOptions = new MarkerOptions();
        newMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        newMarkerOptions.position(currentLocation)
                .title("Ivan je ovdje").snippet("Lutalica");
        mGoogleMap.addMarker(newMarkerOptions);

        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> nearByAddresses = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
                if (nearByAddresses.size() > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    android.location.Address nearestAddress = nearByAddresses.get(0);
                    stringBuilder.append(nearestAddress.getAddressLine(0)).append(", ")
                            .append(nearestAddress.getLocality()).append(", ")
                            .append(nearestAddress.getCountryName());
                    tvLokacija.append(stringBuilder.toString());
                    imeLokacije = nearestAddress.getLocality() + ", "+ nearestAddress.getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ibSlikaj.setOnClickListener(this);
    }
}

