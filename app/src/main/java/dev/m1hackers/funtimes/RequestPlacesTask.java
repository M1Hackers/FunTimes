package dev.m1hackers.funtimes;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * This {@link AsyncTask} takes a location and a list of words, retrieves the nearest places with
 * similar names, and displays them on the {@link com.google.android.gms.maps.MapFragment} of the
 * parent {@link DisplayMapFragment}
 */

public class RequestPlacesTask extends AsyncTask<DisplayMapFragment.requestPlacesTaskParams, Void,
        ArrayList<DisplayMapFragment.Place>> {

    private static final String LOG_TAG = "RequestPlacesTask";
    private static final String PLACES_API_URI = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final int PLACES_SEARCH_RADIUS = 2000;

    private DisplayMapFragment mFragment;

    RequestPlacesTask(DisplayMapFragment displayMapFragment) {
        this.mFragment = displayMapFragment;
    }

    @Override
    protected ArrayList<DisplayMapFragment.Place> doInBackground(DisplayMapFragment.requestPlacesTaskParams... params) {
        DisplayMapFragment.requestPlacesTaskParams param = params[0];
        ArrayList<DisplayMapFragment.Place> resultList = null;
        String keyword = param.keyword;
        double latitude = param.latitude;
        double longitude = param.longitude;

        Log.d(LOG_TAG, "Sending queries to Google Places API Web Service");

        HttpURLConnection conn = null;
        StringBuilder jsonResultString = new StringBuilder();
        try {
            // Build GET Request URL
            String queryURLString = PLACES_API_URI + "?key=" + GlobalSecretKeys.GOOGLE_API_KEY
                    + "&keyword=" + URLEncoder.encode(keyword, "utf8")
                    + "&location=" + String.valueOf(latitude) + "," + String.valueOf(longitude)
                    + "&radius=" + PLACES_SEARCH_RADIUS;

            // Send request
            URL queryURL = new URL(queryURLString);
            conn = (HttpURLConnection) queryURL.openConnection();

            // Read response into jsonResultString
            if (conn.getInputStream() == null) {
                Log.e(LOG_TAG, "No response to request from " + queryURLString);
                throw new IOException(queryURLString);
            }
            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            int numBytes;
            char[] readBuffer = new char[1024];
            while ((numBytes = inputStreamReader.read(readBuffer)) != -1) {
                jsonResultString.append(readBuffer, 0, numBytes);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object from the results
            JSONObject jsonResultObj = new JSONObject(jsonResultString.toString());
            JSONArray jsonResultArray = jsonResultObj.getJSONArray("results");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<>(jsonResultArray.length());
            for (int i = 0; i < jsonResultArray.length(); i++) {
                DisplayMapFragment.Place place = new DisplayMapFragment.Place();
                place.reference = jsonResultArray.getJSONObject(i).getString("reference");
                place.name = jsonResultArray.getJSONObject(i).getString("name");
                JSONObject location = jsonResultArray.getJSONObject(i).getJSONObject("geometry");
                location = location.getJSONObject("location");
                place.lat = Double.parseDouble(location.getString("lat"));
                place.lon = Double.parseDouble(location.getString("lng"));
                Log.i(LOG_TAG, "Adding place: " + place.name + " @ " + place.lat + "," + place.lon);
                resultList.add(place);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing JSON results", e);
        }
        return resultList;
    }

    @Override
    protected void onPostExecute(ArrayList<DisplayMapFragment.Place> results) {
        mFragment.placePointers(results);
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}