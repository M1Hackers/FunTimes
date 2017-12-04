package dev.m1hackers.funtimes;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This {@link AsyncTask} takes a list of Base64 encoded images and queries keywords associated with
 * them from the Google Vision API.
 */

class RequestImageKeywordsTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {

    private static final String LOG_TAG = "RequestImageKey~Task";

    private OnTaskCompleted listener;

    public interface OnTaskCompleted {
        void onTaskCompleted(ArrayList<String> stringArrayList);
    }

    RequestImageKeywordsTask(OnTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected ArrayList<String> doInBackground(ArrayList<String>... inps) {
        ArrayList<String> inp = inps[0];
        ArrayList<String> output = new ArrayList<>();
        for (int i = 0; i < inp.size(); i++) {
            String enc_string = inp.get(i);
            HttpURLConnection conn = null;
            StringBuilder resp = new StringBuilder();

            try {
                URL url = new URL("https://vision.googleapis.com/v1/images:annotate?key=" + GlobalSecretKeys.GOOGLE_API_KEY);
                //Log.i(myTag,sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                Log.d("WelcomeFragment", "Connection established.");

                JSONObject jsonRequestObj = new JSONObject();
                try {
                    JSONObject jsonRequest = new JSONObject();
                    JSONObject jsonFeature = new JSONObject();
                    JSONObject jsonImage = new JSONObject();
                    jsonFeature.put("type", "LABEL_DETECTION");
                    jsonImage.put("content", enc_string);
                    jsonRequest.put("features", jsonFeature);
                    jsonRequest.put("image", jsonImage);
                    jsonRequestObj.put("requests", jsonRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("WelcomeFragment", jsonRequestObj.toString());

                BufferedWriter in = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                Log.d("WelcomeFragment", "Got output stream");
                in.write(jsonRequestObj.toString()); //("{\"requests\":  [{ \"features\":  [ {\"type\": \"LABEL_DETECTION\""+ "}], \"image\": {\"content\": " + enc_string + "}}]}");
                in.flush();
                in.close();
                Log.d("here", in.toString());
                String response = conn.getResponseMessage();

                if (conn.getInputStream() == null) {
                    Log.e("here", url.toString());
                    return output;
                }

                Scanner httpScanner = new Scanner(conn.getInputStream());
                while (httpScanner.hasNext()) {
                    String line = httpScanner.nextLine();
                    resp.append(line);
                }
                httpScanner.close();


            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error processing Places API URL", e);
                return output;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to Places API", e);
                return output;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
//            return resultList;

            try {
                // Create a JSON object hierarchy from the results
                //Log.i(myTag,jsonResults.toString());
                JSONObject jsonObj = new JSONObject(resp.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray("responses");
                JSONArray l = predsJsonArray.getJSONObject(0).getJSONArray("labelAnnotations");
                String p = l.getJSONObject(0).getString("description");

                // Extract the Place descriptions from the results
                output.add(p);
            } catch (JSONException e) {
                Log.d(LOG_TAG, "Error processing JSON results", e);
            }
        }
        return output;
    }

    @Override
    protected void onPostExecute(ArrayList<String> results) {
        Log.i(LOG_TAG, "post");
        listener.onTaskCompleted(results);
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}
