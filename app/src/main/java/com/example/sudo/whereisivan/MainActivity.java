package com.example.sudo.whereisivan;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
    Intent kamera = new Intent();
    Uri slikaUri;
    MediaPlayer mediaPlayer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initialize();

        this.mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.mLocationListener = new SimpleLocationListener();
    }


    private void initialize() {



        mediaPlayer = MediaPlayer.create(this, R.raw.pin);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer media) {
                media.pause();
            }
        });
        this.tvLokacija = (TextView) findViewById(R.id.tvLokacija);
        this.ibSlikaj = (ImageButton) findViewById(R.id.ibSlikaj);


      this.mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fMap);
        this.mMapFragment.getMapAsync(this);

        Log.d("Prijeklikanamapu", "ok");
        this.mCustomMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("klik" , "ok");
                MarkerOptions marker = new MarkerOptions();
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                marker.title("Tamo ću nekad ići!");
                marker.position(latLng);
                mGoogleMap.addMarker(marker);
                mediaPlayer.start();
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Intent openGallery = new Intent();
            openGallery.setAction(android.content.Intent.ACTION_VIEW);
            openGallery.setDataAndType(slikaUri, "image/*");
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, openGallery, 0);


            Notification newPicture = new NotificationCompat.Builder(this)
                    .setContentTitle("Nova slika je uslikana")
                    .setContentText(slikaUri + imeLokacije + ".jpg")
                    .setLights(Color.BLUE, 2000, 1000)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pIntent).build();
            newPicture.flags |= Notification.FLAG_AUTO_CANCEL;

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, newPicture);
        }
    }

    private boolean canBeCalled(Intent implicitIntent) {
        PackageManager manager = this.getPackageManager();
        if (implicitIntent.resolveActivity(manager) != null) {
            return true;
        } else {
            return false;
        }
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
        newMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        newMarkerOptions.position(currentLocation)
                .title("Ivan je ovdje").snippet("Lutalica");
        mGoogleMap.addMarker(newMarkerOptions);

        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<android.location.Address> nearByAddresses = geocoder.getFromLocation(
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

    private void askForPermission() {
        boolean shouldExplain = ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldExplain) {

            this.displayDialog();
        } else {

            tvLokacija.setText("Alo ba, trebam to dopuštenje");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Dopuštenja", "Odobreno. Korisnik pritisnuo dopusti.");
                    } else {
                        Log.d("Dopuštenja", "Odbijeno. Korisnik pritisnuo odbij.");
                        askForPermission();
                    }
                }
        }
    }

    private void displayDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Location permission")
                .setMessage("We display your location and need your permission")
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Permission", "User declined and won't be asked again.");
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Permission", "Permission requested because of the explanation.");
                        requestPermission();
                        dialog.cancel();
                    }
                })
                .show();
    }
}

