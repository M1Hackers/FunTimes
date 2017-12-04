package dev.m1hackers.funtimes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

/**
 * The {@link Fragment} which the user first sees.
 */
public class WelcomeFragment extends CustomFragment implements RequestImageKeywordsTask.OnTaskCompleted {

    private static final Hashtable<String, Integer> requestCodeMap = new Hashtable<String, Integer>() {{
        put(Manifest.permission.READ_EXTERNAL_STORAGE, 1);
        put(Manifest.permission.ACCESS_FINE_LOCATION, 2);
    }};
    private static final String LOG_TAG = "WelcomeFragment";
    protected MainActivity thisActivity;
    protected WelcomeFragment thisFragment;
    Spinner imgNumSpinner;
    Button executeButton;
    private OnMapRequestListener mapDisplayCallback;
    private ArrayList<String> imgKeywords;

    public WelcomeFragment() {
        thisFragment = this;
    }

    @SuppressWarnings("SameParameterValue")
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    /* Return point for RequestImageKeywordsTask */
    @Override
    public void onTaskCompleted(ArrayList<String> stringArrayList) {
        imgKeywords = stringArrayList;

        // Ask for permissions if we are installed on Android 6.0+
        if(Build.VERSION.SDK_INT >= 23 && !WelcomeFragment.checkPermission(thisActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            WelcomeFragment.requestPermission(thisFragment,Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            mapDisplayCallback.onMapRequest(stringArrayList);
        }
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
        executeButton = V.findViewById(R.id.execute_button);

        // Set up Spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> imgNumSpinnerAdapter = ArrayAdapter.createFromResource(
                thisActivity, R.array.img_num_array, R.layout.custom_spinner_item);
        // Specify the layout to use when the list of choices appears
        imgNumSpinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        imgNumSpinner.setAdapter(imgNumSpinnerAdapter);

        // Handle button presses
        executeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ask for permissions if we are installed on Android 6.0+
                if(Build.VERSION.SDK_INT >= 23 && !WelcomeFragment.checkPermission(thisActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        WelcomeFragment.requestPermission(thisFragment,Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    startImageQuery(Integer.parseInt(imgNumSpinner.getSelectedItem().toString()));
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

        if(activity instanceof MainActivity) {
            thisActivity = (MainActivity) activity;
        }
    }

    /* Return point for permissions dialog, toasts the result. */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(LOG_TAG,"Executing onRequestPermissionResult");
        if (requestCode == requestCodeMap.get(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(LOG_TAG, "Storage permission granted");
                startImageQuery(Integer.parseInt(imgNumSpinner.getSelectedItem().toString()));
            } else {
                Log.i(LOG_TAG, "Storage permission denied");
            }
        }
        else if(requestCode == requestCodeMap.get(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(LOG_TAG, "Location permission granted");
                mapDisplayCallback.onMapRequest(imgKeywords);
            } else {
                Log.i(LOG_TAG, "Location permission denied");
            }
        }
        else {
            Log.d(LOG_TAG, "Nonexistent permission");
        }
    }

    /**
     * Retrieves the last <code>picNum</code> images from the camera folder and executes a
     * {@link RequestImageKeywordsTask} to process them (uses helper AsyncTask to not block UI thread)
     * @param picNum Number of most recent pictures to be uploaded
     */
    private void startImageQuery(int picNum) {
        StartImageQueryTask mTask = new StartImageQueryTask();
        mTask.execute(picNum);
    }

    private File[] listDirectoryRecursive(File dir, boolean ignoreHidden, ArrayList<String> ignoreList) {
        // Check that this is a directory that we can read
        if (!dir.exists() || !dir.canRead() || !dir.isDirectory()
                || (ignoreHidden && dir.isHidden())) return null;

        // List files
        File[] fileArray = dir.listFiles();
        List<File> fileList = new ArrayList<>(Arrays.asList(fileArray));
        // Iterate through files (and directories) in the directory
        for (int i = 0; i < fileList.size(); i++) {
            File cFile = fileList.get(i);
            if (cFile.isDirectory()) {
                if (ignoreList != null && ignoreList.contains(cFile.getName())) {
                    fileList.remove(i);
                } else {
                    File[] subDirFiles = listDirectoryRecursive(fileList.get(i), ignoreHidden, ignoreList);
                    fileList.remove(i);
                    if (subDirFiles != null) fileList.addAll(Arrays.asList(subDirFiles));
                }
                i--;
            }
        }

        return fileList.toArray(new File[]{});
    }

    public interface OnMapRequestListener {
        void onMapRequest(ArrayList<String> stringArrayList);
    }

    @SuppressLint("StaticFieldLeak")
    private class StartImageQueryTask extends AsyncTask<Integer,Void,Void> {

        @Override
        protected Void doInBackground(Integer... integers) {
            int picNum = integers[0];
            final int MAX_IMAGE_SIZE = 300;
            final int JPEG_QUALITY = 70;

            // Check if there is an external storage mounted and accessible
            Log.d(LOG_TAG, "Gathering picture list...");
            String storageState = Environment.getExternalStorageState();
            if(!(Environment.MEDIA_MOUNTED.equals(storageState) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState))) {
                Toast.makeText(WelcomeFragment.this.getActivity(), getString(R.string.no_storage_msg),
                        Toast.LENGTH_LONG).show();
                return null;
            }

            // Get directory descriptor
            File DCIMDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File imgDirectory = new File(DCIMDirectory.getPath() + "/Camera");

            // Check that there is a DCIM/Camera directory and we can read it
            File[] imgList = listDirectoryRecursive(imgDirectory, true, null);
            if(imgList == null) {
                Toast.makeText(WelcomeFragment.this.getActivity(), getString(R.string.no_pictures_msg),
                        Toast.LENGTH_LONG).show();
                return null;
            }

            // Build an array containing last modified date and index in the file array
            List<List<Long>> dateArray = new ArrayList<>();
            for(int i = 0; i < imgList.length; i++) {
                List<Long> dateArrayElt = new ArrayList<>();
                dateArrayElt.add((long) i);
                dateArrayElt.add(imgList[i].lastModified());
                dateArray.add(dateArrayElt);
            }

            // Sort array by last modified date (reverse == most recent first)
            Collections.sort(dateArray, new Comparator<List<Long>>() {
                @Override
                public int compare(List<Long> o1, List<Long> o2) {
                    return o2.get(1).compareTo(o1.get(1));
                }
            });

            // Create an array for files that have to be uploaded
            List<File> selectedImgList = new ArrayList<>();
            for(int i = 0; i < picNum; i++) {
                int imgIndex = dateArray.get(i).get(0).intValue();
                Log.i(LOG_TAG, "Adding picture: " + imgList[imgIndex].getName());
                selectedImgList.add(imgList[imgIndex]);
            }

            // Loop through filtered file list
            FileInputStream fis;
            ArrayList<String> encodedImageList = new ArrayList<>();
            for(File imgFile: selectedImgList) {
                // Open image
                try{
                    fis = new FileInputStream(imgFile);
                } catch(FileNotFoundException e) {
                    Log.e(LOG_TAG, "File not found (it has probably been deleted since the parent directory was listed)");
                    e.printStackTrace();
                    continue;
                }

                // Decode, resize, and compress image
                Bitmap bm = BitmapFactory.decodeStream(fis);
                bm = scaleDown(bm,MAX_IMAGE_SIZE,false);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG,JPEG_QUALITY,baos);
                String encImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                encodedImageList.add(encImage);
            }

            // Send images to RequestImageKeywordsTask
            RequestImageKeywordsTask mTask = new RequestImageKeywordsTask(thisFragment);
            //noinspection unchecked
            mTask.execute(encodedImageList);
            return null;
        }
    }

}
