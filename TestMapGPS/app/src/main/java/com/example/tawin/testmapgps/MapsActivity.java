package com.example.tawin.testmapgps;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Marker mMarker;
    LocationManager lm;
    double lat, lng;
    ArrayList<Data_Event> countries = new ArrayList<Data_Event>();
    private String path = "http://192.168.0.5/ProjectJ/test.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Button btnLoc = (Button) findViewById(R.id.gpsbut);
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{//GPS MAP
                Manifest.permission.ACCESS_FINE_LOCATION}, 123);//GPS MAP

        Button alertbutton = (Button)findViewById(R.id.PutAlert);
        alertbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent r = new Intent(getApplicationContext(), putAlert.class);
                startActivity(r);

            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     */
    LocationListener listener = new LocationListener() {
        public void onLocationChanged(Location loc) {
            LatLng coordinate = new LatLng(loc.getLatitude()
                    , loc.getLongitude());
            lat = loc.getLatitude();
            lng = loc.getLongitude();

            if (mMarker != null)
                mMarker.remove();

            mMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    coordinate, 18));

        }

        public void onStatusChanged(String provider, int status
                , Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    public void onResume() {
        super.onResume();

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isNetwork =
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGPS =
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isNetwork) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER
                    , 4000, 10, listener);
            Location loc = lm.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
            if(loc != null) {
                lat = loc.getLatitude();
                lng = loc.getLongitude();
            }
        }

        if(isGPS) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER
                    , 5000, 10, listener);
            Location loc = lm.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);
            if(loc != null) {
                lat = loc.getLatitude();
                lng = loc.getLongitude();
                Toast.makeText(getApplicationContext(),"GPS Lat = "+lat+"\n lon = "+lng,Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void onPause() {
        super.onPause();
        lm.removeUpdates(listener);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {//GPS MAP
        mMap = googleMap;
        final LatLng[] sydney2 = new LatLng[1];
        GpsTracker gt = new GpsTracker(getApplicationContext());
        MapsActivity callGPS = new MapsActivity();
        Location l = gt.getLocation();
        LatLng sydney = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,18));
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
    }//GPS MAP
}
