package dev.m1hackers.funtimes;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * This {@link AsyncTask} takes a list of Base64 encoded images and queries keywords associated with
 * them from the Google Vision API.
 */

class RequestImageKeywordsTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {

    private static final String LOG_TAG = "RequestImageKey~Task";
    private static final String VISION_API_URI = "https://vision.googleapis.com/v1/images:annotate";

    private OnTaskCompleted listener;

    RequestImageKeywordsTask(OnTaskCompleted listener) {
        this.listener = listener;
    }

    @SafeVarargs
    @Override
    protected final ArrayList<String> doInBackground(ArrayList<String>... params) {
        ArrayList<String> encodedImageList = params[0];
        ArrayList<String> keywordList = new ArrayList<>();

        Log.d(LOG_TAG, "Sending queries to Google Cloud Vision API");

        // Iterate through list of (Base64 encoded) images
        for (int i = 0; i < encodedImageList.size(); i++) {
            String encodedImage = encodedImageList.get(i);
            HttpURLConnection conn = null;
            StringBuilder jsonResultString = new StringBuilder();

            try {
                // Build request URL
                String requestURLString = VISION_API_URI + "?key=" + GlobalSecretKeys.GOOGLE_API_KEY;
                URL requestURL = new URL(requestURLString);

                // Connect
                conn = (HttpURLConnection) requestURL.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                // Build request JSON
                JSONObject jsonRequestObj = new JSONObject();
                try {
                    JSONObject jsonRequest = new JSONObject();
                    JSONObject jsonFeature = new JSONObject();
                    JSONObject jsonImage = new JSONObject();
                    jsonFeature.put("type", "LABEL_DETECTION");
                    jsonImage.put("content", encodedImage);
                    jsonRequest.put("features", jsonFeature);
                    jsonRequest.put("image", jsonImage);
                    jsonRequestObj.put("requests", jsonRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Send POST request
                BufferedWriter in = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                in.write(jsonRequestObj.toString());
                in.flush();
                in.close();

                // Read response into jsonResultString
                if (conn.getInputStream() == null) {
                    Log.e(LOG_TAG, "No response to POST request from " + requestURLString);
                    return keywordList;
                }
                BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                int numBytes;
                char[] readBuffer = new char[1024];
                while ((numBytes = inputStreamReader.read(readBuffer)) != -1) {
                    jsonResultString.append(readBuffer, 0, numBytes);
                }


            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error processing Maps API URL", e);
                return keywordList;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to Maps API", e);
                return keywordList;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
                // Parse received JSON object
                JSONObject jsonResultObj = new JSONObject(jsonResultString.toString());
                JSONArray jsonResponseArray = jsonResultObj.getJSONArray("responses");
                JSONArray jsonLabelArray = jsonResponseArray.getJSONObject(0).getJSONArray("labelAnnotations");
                // Only topmost label (with highest score)
                String jsonDescriptionString = jsonLabelArray.getJSONObject(0).getString("description");
                // Add image keyword to output
                Log.i(LOG_TAG, "Adding keyword: " + jsonDescriptionString);
                keywordList.add(jsonDescriptionString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error processing JSON results", e);
            }
        }
        return keywordList;
    }

    @Override
    protected void onPostExecute(ArrayList<String> results) {
        listener.onTaskCompleted(results);
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(ArrayList<String> stringArrayList);
    }
}
