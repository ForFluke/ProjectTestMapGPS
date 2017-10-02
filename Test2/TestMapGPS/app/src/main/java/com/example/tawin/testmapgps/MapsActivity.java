package com.example.tawin.testmapgps;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private String path = "http://10.58.124.72/ProjectJ/test.php";
    Marker mMarker;
    LocationManager lm;
    double lat, lng;
    ArrayList<Data_Event> countries = new ArrayList<Data_Event>();


    /**
     * A {@link LocationSource} which reports a new location whenever a user long presses the map
     * at
     * the point at which a user long pressed the map.
     */
    private static class LongPressLocationSource implements LocationSource, GoogleMap.OnMapLongClickListener {

        private OnLocationChangedListener mListener;

        /**
         * Flag to keep track of the activity's lifecycle. This is not strictly necessary in this
         * case because onMapLongPress events don't occur while the activity containing the map is
         * paused but is included to demonstrate best practices (e.g., if a background service were
         * to be used).
         */
        private boolean mPaused;

        @Override
        public void activate(OnLocationChangedListener listener) {
            mListener = listener;
        }

        @Override
        public void deactivate() {
            mListener = null;
        }

        @Override
        public void onMapLongClick(LatLng point) {
            if (mListener != null && !mPaused) {
                Location location = new Location("LongPressLocationProvider");
                location.setLatitude(point.latitude);
                location.setLongitude(point.longitude);
                location.setAccuracy(100);
                mListener.onLocationChanged(location);
            }
        }

        public void onPause() {
            mPaused = true;
        }

        public void onResume() {
            mPaused = false;
        }
    }

    private GoogleMap mMap;
    private EditText locationSearch;

    private Geocoder geocoder;

    private LocationManager locationManager;

    private LongPressLocationSource mLocationSouce;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button alertbutton = (Button)findViewById(R.id.PutAlert);
        alertbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent r = new Intent(getApplicationContext(), putAlert.class);
                startActivity(r);

            }
        });

        locationSearch = (EditText) findViewById(R.id.editText);

        geocoder = new Geocoder(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final LatLng[] sydney = new LatLng[1];
        enableMyLocation();

        mMap.setLocationSource(mLocationSouce);
        mMap = googleMap;
        final LatLng[] sydney2 = new LatLng[1];
        GpsTracker gt = new GpsTracker(getApplicationContext());
        MapsActivity callGPS = new MapsActivity();
        Location l = gt.getLocation();
        LatLng sydney3 = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(sydney3).title(" You are here "));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney3));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney3,18));



        if( l == null){
            Toast.makeText(getApplicationContext(),"GPS unable to get Value",Toast.LENGTH_SHORT).show();
        }else {

            double lat = l.getLatitude();
            double lng = l.getLongitude();
            Toast.makeText(getApplicationContext(),"GPS Lat = "+lat+"\n lon = "+lng,Toast.LENGTH_SHORT).show();

            // Add a marker in Sydney and move the camera


            JsonArrayRequest req = new JsonArrayRequest(path,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d(TAG, response.toString());
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject jsonobject = response.getJSONObject(i);
                                    String title = jsonobject.getString("EventName");
                                    double longitude = jsonobject.getDouble("lng");
                                    double latitude = jsonobject.getDouble("lat");
                                    Log.d(TAG, "EventName:" + title);
                                    Log.d(TAG, "lng" + longitude);
                                    Log.d(TAG, "lat" + latitude);
                                    sydney2[0] = new LatLng(latitude, longitude);
                                    mMap.addMarker(new MarkerOptions().position(sydney2[0]).title(title));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney2[0]));
                                    Data_Event countryObj = new Data_Event();
                                    countries.add(countryObj);
                                }
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney2[0],15));

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MapsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }

            });
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(req);



//            Intent r = new Intent(getApplicationContext(), MapsActivity.class);
//            startActivity(r);
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(latitude, longitude), 21));

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils
                .PermissionDeniedDialog
                .newInstance(true)
                .show(getSupportFragmentManager(), "dialog");
    }

    public void onMapSearch(View view) {
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location.isEmpty())
            return;
        try {
            addressList = geocoder.getFromLocationName(location, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addressList == null || addressList.isEmpty())
            return;

        Address address = addressList.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

}
