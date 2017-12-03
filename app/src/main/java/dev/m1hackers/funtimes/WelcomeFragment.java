package dev.m1hackers.funtimes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


import java.io.BufferedWriter;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class WelcomeFragment extends CustomFragment {

    public ArrayList<String> categories = null;

    private static final String API_key = "AIzaSyAPPm6FmMfhXHxKoScqLuRcD-9H3QSm8f4";

    private static final Hashtable<String, Integer> requestCodeMap = new Hashtable<String, Integer>() {{
        put(Manifest.permission.READ_EXTERNAL_STORAGE, 1);
        put(Manifest.permission.ACCESS_FINE_LOCATION, 2);
    }};
    private static final String LOG_TAG = "WelcomeFragment";

    Spinner imgNumSpinner;
    Button uploadButton;
    Button displayMapButton;
    private OnMapRequestListener mapDisplayCallback;
    private OnImgListReadyListener imgListReadyCallback;
    protected MainActivity thisActivity;
    protected WelcomeFragment thisFragment;

    public interface OnImgListReadyListener {
        void onImgListReady(File[] imgList);
    }

    public interface OnMapRequestListener {
        void onMapRequest();
    }

    public WelcomeFragment() {
        thisFragment = this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View V = getView();
        if (V == null) return;
        imgNumSpinner = V.findViewById(R.id.img_num_spinner);
        uploadButton = V.findViewById(R.id.upload_button);
        displayMapButton = V.findViewById(R.id.display_map_button);

        // Set up Spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> imgNumSpinnerAdapter = ArrayAdapter.createFromResource(
                thisActivity, R.array.img_num_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        imgNumSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imgNumSpinner.setAdapter(imgNumSpinnerAdapter);

        // Handle button presses
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check whether we are installed on Android 6.0+
                if(Build.VERSION.SDK_INT >= 23) {
                    if(WelcomeFragment.checkPermission(thisActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        uploadPictures(Integer.parseInt(imgNumSpinner.getSelectedItem().toString()));
                    } else {
                        WelcomeFragment.requestPermission(thisFragment,Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }
            }
        });
        displayMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("WelcomeFragment","Map button clicked");
                // Check whether we are installed on Android 6.0+
                if(Build.VERSION.SDK_INT >= 23) {
                    if(WelcomeFragment.checkPermission(thisActivity,Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d("WelcomeFragment","Displaying map...");
                        mapDisplayCallback.onMapRequest();
                    } else {
                        WelcomeFragment.requestPermission(thisFragment,Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mapDisplayCallback = (OnMapRequestListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMapRequestListener");
        }

        try {
            imgListReadyCallback = (OnImgListReadyListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnImgListReadyListener");
        }
        thisActivity = (MainActivity) activity;
    }

    /* Return point for permissions dialog, toasts the result. */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(LOG_TAG,"Executing onRequestPermissionResult");
        if (requestCode == requestCodeMap.get(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG,"Storage permission granted.");
                uploadPictures(Integer.parseInt(imgNumSpinner.getSelectedItem().toString()));
            } else {
                Log.d(LOG_TAG,"Location permission denied.");
            }
        }
        else if(requestCode == requestCodeMap.get(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG,"Location permission granted.");
                Log.d(LOG_TAG,"Displaying map...");
                mapDisplayCallback.onMapRequest();
            } else {
                Log.d(LOG_TAG,"Location permission denied.");
            }
        }
        else {
            Log.d(LOG_TAG,"Nonexistent permission.");
        }
    }

    /**
     * Uploads the last <code>picNum</code>
     * @param picNum Number of most recent pictures to be uploaded
     */
    private void uploadPictures(int picNum) {
        // Check if there is an external storage mounted and accessible
        Log.d(LOG_TAG,"Gathering picture list.");
        String storageState = Environment.getExternalStorageState();
        if(!(Environment.MEDIA_MOUNTED.equals(storageState) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState))) {
            Toast.makeText(WelcomeFragment.this.getActivity(), getString(R.string.no_storage_msg),
                    Toast.LENGTH_LONG).show();
            return;
        }

        File dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File imgDirectory = new File(dcimDirectory.getPath() + "/Camera");

        // Check that there is a DCIM/Camera directory and we can read it
        // List all images in DCIM directory
        File[] imgList = listDirectoryRecursive(imgDirectory, ".thumbnails");
        // Check that there was a DCIM (camera pictures) directory and we could read it
        if(imgList == null) {
            Toast.makeText(WelcomeFragment.this.getActivity(), getString(R.string.no_pictures_msg),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Build an array containing last modified date and index in the file array
        List<List<Long>> dateArray = new ArrayList<>();
        for(int i = 0; i < imgList.length; i++) {
            List<Long> dateArrayElt = new ArrayList<>();
            dateArrayElt.add((long) i);
            dateArrayElt.add(imgList[i].lastModified());
            dateArray.add(dateArrayElt);
        }

        // Sort array by last modified date
        Collections.sort(dateArray, new Comparator<List<Long>>() {
            @Override
            public int compare(List<Long> o1, List<Long> o2) {
                return o1.get(1).compareTo(o2.get(1));
            }
        });

        // Create an array for files that have to be uploaded
        List<File> selectedImgList = new ArrayList<>();
        for(int i = 0; i < picNum; i++) {
            int imgIndex = dateArray.get(i).get(0).intValue();
            selectedImgList.add(imgList[imgIndex]);
        }

        uploadFiles(selectedImgList.toArray(new File[]{}));

    }

    private File[] listDirectoryRecursive(File dir, String ignore) {
        // Check that this is a directory that we can read
        if(!dir.exists() || !dir.canRead() || !dir.isDirectory()) return null;

        // List files
        File[] fileArray = dir.listFiles();
        List<File> fileList = new ArrayList<>(Arrays.asList(fileArray));
        // Iterate through files (and directories) in the directory
        for(int i = 0; i < fileList.size(); i++) {
            File cFile = fileList.get(i);
            if(cFile.isDirectory()) {
                if(ignore != null && ignore.contains(cFile.getName())) {
                    fileList.remove(i);
                } else {
                    File[] subDirFiles = listDirectoryRecursive(fileList.get(i), null);
                    fileList.remove(i);
                    i--;
                    if (subDirFiles != null) fileList.addAll(Arrays.asList(subDirFiles));
                }
            }
        }

        return fileList.toArray(new File[]{});
    }

    /**
     * Uploads files to our server instance in the Google Cloud
     * @param files array of {@link File}s to upload
     */
    private void uploadFiles(File[] files) {
        Log.d(LOG_TAG,"Sending files.");
        StringBuilder message = new StringBuilder();

        String message = "";
        String returned = "";
        FileInputStream fis = null;
        ArrayList<String> return_list = new ArrayList<>(3);
        for(File file: files) {
            message.append(file.getPath());
            message.append("\n");
            message += file.getPath();
            message += "\n";
            try{
                fis = new FileInputStream(file);
            }catch(FileNotFoundException e){
                Log.e("herehere","yike");
                e.printStackTrace();
            }
            Log.e("here",fis.toString());

            Bitmap bm = BitmapFactory.decodeStream(fis);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try{
                bm = scaleDown(bm,300,false);
                bm.compress(Bitmap.CompressFormat.JPEG,70,baos);
                byte[] b = baos.toByteArray();
                String encImage = Base64.encodeToString(b, Base64.DEFAULT);
                Log.e("here",encImage);
//            Base64.de
                return_list.add(encImage);
                Log.e("here","encoded "+ Integer.toString(encImage.length()));
            }catch(NullPointerException e){
                e.printStackTrace();
                Log.e("here","ri");
            }
        }
        Toast.makeText(WelcomeFragment.this.getActivity(), message.toString(),Toast.LENGTH_LONG).show();

        // TODO: implement file uploading.
        //imgListReadyCallback.onImgListReady(files);
        Log.i("herehere",returned);
//        return returned;
        PostRequest l = new PostRequest();
        l.execute(return_list);
    }
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    private class PostRequest extends AsyncTask<ArrayList<String>,Void,ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>...inps){
            ArrayList<String> inp = inps[0];
            ArrayList<String> output = new ArrayList<>();
            for(int i=0;i<inp.size();i++) {
                String enc_string = inp.get(i);
                HttpURLConnection conn = null;
                String resp = "";

                try {
                    StringBuilder sb = new StringBuilder("https://vision.googleapis.com/v1/images:annotate?key=" + API_key);
                    URL url = new URL(sb.toString());
                    //Log.i(myTag,sb.toString());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    Log.d("WelcomeFragment","Connection established.");

                    JSONObject jsonRequestObj = new JSONObject();
                    try {
                        JSONObject jsonRequest = new JSONObject();
                        JSONObject jsonFeature = new JSONObject();
                        JSONObject jsonImage = new JSONObject();
                        jsonFeature.put("type","LABEL_DETECTION");
                        jsonImage.put("content",enc_string);
                        jsonRequest.put("features",jsonFeature);
                        jsonRequest.put("image",jsonImage);
                        jsonRequestObj.put("requests",jsonRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("WelcomeFragment",jsonRequestObj.toString());

                    BufferedWriter in = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                    Log.d("WelcomeFragment","Got output stream");
                    in.write(jsonRequestObj.toString()); //("{\"requests\":  [{ \"features\":  [ {\"type\": \"LABEL_DETECTION\""+ "}], \"image\": {\"content\": " + enc_string + "}}]}");
                    in.flush();
                    in.close();
                    Log.d("here",in.toString());
                    String response = conn.getResponseMessage();

                    if (conn.getInputStream() == null) {
                        Log.e("here",url.toString());
                        return output;
                    }

                    Scanner httpScanner = new Scanner(conn.getInputStream());
                    while (httpScanner.hasNext()) {
                        String line = httpScanner.nextLine();
                        resp += line;
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
                    JSONObject jsonObj = new JSONObject(resp);
                    JSONArray predsJsonArray = jsonObj.getJSONArray("responses");
                    JSONArray l = predsJsonArray.getJSONObject(0).getJSONArray("labelAnnotations");
                    String p = l.getJSONObject(0).getString("description");

                    // Extract the Place descriptions from the results
                    output.add(p);
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "Error processing JSON results", e);
                }
            }
                categories = output;
//            Log.d("WelcomeFragment",output);
                return output;
        }

        @Override
        protected void onPostExecute(ArrayList<String> results) {
            Log.i(LOG_TAG,"post");
            DisplayMapFragment.categories = results;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}
