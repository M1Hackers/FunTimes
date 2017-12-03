package dev.m1hackers.funtimes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class WelcomeFragment extends Fragment {

    private static final Hashtable<String, Integer> requestCodeMap = new Hashtable<String, Integer>() {{
        put(Manifest.permission.READ_EXTERNAL_STORAGE, 1);
        put(Manifest.permission.ACCESS_FINE_LOCATION, 2);
    }};

    Spinner imgNumSpinner;
    Button uploadButton;
    Button displayMapButton;
    private OnMapRequestListener mapDisplayCallback;

    public interface OnMapRequestListener {
        void onMapRequest();
    }

    public WelcomeFragment() {
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
                this.getActivity(), R.array.img_num_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        imgNumSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imgNumSpinner.setAdapter(imgNumSpinnerAdapter);

        // Handle button presses
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check whether we are installed on Android 6.0+
                if(Build.VERSION.SDK_INT >= 23) {
                    if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        uploadPictures(Integer.parseInt(imgNumSpinner.getSelectedItem().toString()));
                    } else {
                        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
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
                    if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d("WelcomeFragment","Displaying map...");
                        mapDisplayCallback.onMapRequest();
                    } else {
                        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
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
                    + " must implement OnHeadlineSelectedListener");
        }
    }


    /**
     * Checks if we have some permission.
     * @param permissionId the permission to be checked, a {@link String} from {@link Manifest.permission}
     * @return true if the permission is granted, false if not
     */
    private boolean checkPermission(String permissionId) {
        int result = ContextCompat.checkSelfPermission(WelcomeFragment.this.getActivity(),permissionId);
        return (result == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Displays request dialog for permission.
     * @param permissionId the permission requested, a {@link String} from {@link Manifest.permission}
     */
    private void requestPermission(String permissionId) {
        ActivityCompat.requestPermissions(this.getActivity(), new String[]{permissionId},requestCodeMap.get(permissionId));
    }

    /* Return point for permissions dialog, toasts the result. */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == requestCodeMap.get(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (BuildConfig.DEBUG) Toast.makeText(WelcomeFragment.this.getActivity(),
                        getString(R.string.permission_granted_msg), Toast.LENGTH_LONG).show();
                uploadPictures(Integer.parseInt(imgNumSpinner.getSelectedItem().toString()));
            } else {
                if (BuildConfig.DEBUG) Toast.makeText(WelcomeFragment.this.getActivity(),
                        getString(R.string.permission_denied_msg), Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == requestCodeMap.get(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (BuildConfig.DEBUG) Toast.makeText(WelcomeFragment.this.getActivity(),
                        getString(R.string.permission_granted_msg), Toast.LENGTH_LONG).show();
                Log.d("WelcomeFragment","Displaying map...");
                mapDisplayCallback.onMapRequest();
            } else {
                if (BuildConfig.DEBUG) Toast.makeText(WelcomeFragment.this.getActivity(),
                        getString(R.string.permission_denied_msg), Toast.LENGTH_LONG).show();
            }
        }
        else {
            Log.d("WelcomeFragment","Nonexistent permission.");
        }
    }

    /**
     * Uploads the last <code>picNum</code>
     * @param picNum Number of most recent pictures to be uploaded
     */
    private void uploadPictures(int picNum) {
        // Check if there is an external storage mounted and accessible
        String storageState = Environment.getExternalStorageState();
        if(!(Environment.MEDIA_MOUNTED.equals(storageState) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState))) {
            Toast.makeText(WelcomeFragment.this.getActivity(), getString(R.string.no_storage_msg),
                    Toast.LENGTH_LONG).show();
            return;
        }

        File imgDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        // Check that there is a DCIM (camera pictures) directory and we can read it

        // List all images in DCIM directory
        File[] imgList = listDirectoryRecursive(imgDirectory);
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

    private File[] listDirectoryRecursive(File dir) {
        // Check that this is a directory that we can read
        if(!dir.exists() || !dir.canRead() || !dir.isDirectory()) return null;

        // List files
        File[] fileArray = dir.listFiles();
        List<File> fileList = new ArrayList<>(Arrays.asList(fileArray));
        // Iterate through files (and directories) in the directory
        for(int i = 0; i < fileList.size(); i++) {
            if(fileList.get(i).isDirectory()) {
                File[] subDirFiles = listDirectoryRecursive(fileList.get(i));
                fileList.remove(i);
                i--;
                if(subDirFiles != null) fileList.addAll(Arrays.asList(subDirFiles));
            }
        }

        return fileList.toArray(new File[]{});
    }

    /**
     * Uploads files to our server instance in the Google Cloud
     * @param files array of {@link File}s to upload
     */
    private void uploadFiles(File[] files) {
        // TODO: implement file uploading.
    }
}
