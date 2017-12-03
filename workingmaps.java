package com.example.chsue.googlemapstest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import android.location.Location;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.net.URLEncoder;
import java.io.IOException;
/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.
 */
public class MapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

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
    private String myTag = "herehere";
    private boolean mPermissionDenied = false;
    private FusedLocationProviderClient mFusedLocationClient;
    public class Inputobj{
        String keyword;
        Double lat;
        Double lon;
    }
    private GoogleMap mMap;
    protected Location mLastLocation;
    public static GoogleMap m = null;
    ArrayList<Place> results = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        m = map;
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d("permission", "Storage permission granted");
        } else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            Inputobj inp = new Inputobj();
                            inp.keyword = "books";
                            inp.lat = mLastLocation.getLatitude();
                            inp.lon = mLastLocation.getLongitude();
                            longop l = new longop();
                            l.execute(inp);
                        }
                        else {
                        }
                    }
                });


//        Toast.makeText(this, "Latitude:"+mLastLocation.getLatitude(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */

    private void placePointers(ArrayList<Place> results){
        if(results!=null){
            Log.i(myTag,"now here");
            for (int i = 0; i < results.size(); i++) {
                Place current_place = results.get(i);
                LatLng coord = new LatLng(current_place.lat, current_place.lon);
                Log.i(myTag,current_place.name+Double.toString(current_place.lat)+","+Double.toString(current_place.lon));
                m.addMarker(new MarkerOptions().position(coord).title(current_place.name));

            }
        }
    }
        /* Displays request dialog for permission. */
    private void requestPermission(String permissionId) {
        ActivityCompat.requestPermissions(this, new String[]{permissionId},LOCATION_PERMISSION_REQUEST_CODE);
    }
    /* Check if we have a certain permission. */
    private boolean checkPermission(String permissionId) {
        int result = ContextCompat.checkSelfPermission(MapsActivity.this,permissionId);
        return (result == PackageManager.PERMISSION_GRANTED);
    }
    private void enableMyLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d("permission", "Storage permission granted");
        } else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(BuildConfig.DEBUG) Toast.makeText(MapsActivity.this,
                            getString(R.string.permission_granted_toast), Toast.LENGTH_LONG).show();
                    enableMyLocation();
                } else {
                    if(BuildConfig.DEBUG) Toast.makeText(MapsActivity.this,
                            getString(R.string.permission_denied_toast), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    private static final String LOG_TAG = "ExampleApp";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";

    private static final String TYPE_DETAILS = "/details";
    private static final String TYPE_SEARCH = "/nearbysearch";

    private static final String OUT_JSON = "/json";

    // KEY!
    private static final String API_KEY = "AIzaSyCoyESSSVsupzauMVKA24FDf_DC4ETsimI";
    public static class Place{
        String reference;
        double lat;
        double lon;
        String name;
        String formatted_address;
    }

    private class longop extends AsyncTask<Inputobj,Void,ArrayList<Place>> {
        @Override
        protected ArrayList<Place> doInBackground(Inputobj ...inps){
            Inputobj inp = inps[0];
            ArrayList<Place> resultList = null;
            String keyword = inp.keyword;
            double lat = inp.lat;
            double lng = inp.lon;

            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder(PLACES_API_BASE);
                sb.append(TYPE_SEARCH);
                sb.append(OUT_JSON);
                sb.append("?sensor=false");
                sb.append("&key=" + API_KEY);
                sb.append("&keyword=" + URLEncoder.encode(keyword, "utf8"));
                sb.append("&location=" + String.valueOf(lat) + "," + String.valueOf(lng));
                sb.append("&radius=" + String.valueOf(2000));
                System.out.println(sb.toString());
                URL url = new URL(sb.toString());
                //Log.i(myTag,sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));


                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }


            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error processing Places API URL", e);
                return resultList;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to Places API", e);
                return resultList;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
//            return resultList;

        try {
            // Create a JSON object hierarchy from the results
            //Log.i(myTag,jsonResults.toString());
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("results");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<Place>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                Place place = new Place();
                place.reference = predsJsonArray.getJSONObject(i).getString("reference");
                place.name = predsJsonArray.getJSONObject(i).getString("name");
                JSONObject location = predsJsonArray.getJSONObject(i).getJSONObject("geometry");
                location = location.getJSONObject("location");
                place.lat = Double.parseDouble(location.getString("lat"));
                place.lon = Double.parseDouble(location.getString("lng"));
                Log.i(myTag,place.name);
                resultList.add(place);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing JSON results", e);
        }
        results = resultList;
        return resultList;
        }

        @Override
        protected void onPostExecute(ArrayList<Place> results) {
            Log.i(myTag,"post");
            placePointers(results);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


//    public static Place details(String reference) {
//        HttpURLConnection conn = null;
//        StringBuilder jsonResults = new StringBuilder();
//        try {
//            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
//            sb.append(TYPE_DETAILS);
//            sb.append(OUT_JSON);
//            sb.append("?sensor=false");
//            sb.append("&key=" + API_KEY);
//            sb.append("&reference=" + URLEncoder.encode(reference, "utf8"));
//
//            URL url = new URL(sb.toString());
//            conn = (HttpURLConnection) url.openConnection();
//            InputStreamReader in = new InputStreamReader(conn.getInputStream());
//
//            // Load the results into a StringBuilder
//            int read;
//            char[] buff = new char[1024];
//            while ((read = in.read(buff)) != -1) {
//                jsonResults.append(buff, 0, read);
//            }
//        } catch (MalformedURLException e) {
//            Log.e(LOG_TAG, "Error processing Places API URL", e);
//            return null;
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "Error connecting to Places API", e);
//            return null;
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }
//
//        Place place = null;
//        try {
//            // Create a JSON object hierarchy from the results
//            JSONObject jsonObj = new JSONObject(jsonResults.toString()).getJSONObject("result");
//
//            place = new Place();
//            place.name = jsonObj.getString("name");
//            place.formatted_address = jsonObj.getString("formatted_address");
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, "Error processing JSON results", e);
//        }
//
//        return place;
//    }
}
