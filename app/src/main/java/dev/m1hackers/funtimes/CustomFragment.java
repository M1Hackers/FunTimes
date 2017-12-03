package dev.m1hackers.funtimes;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;

/**
 * Wrapper for {@link Fragment} with permission helper methods.
 */

abstract class CustomFragment extends Fragment {

    protected static final HashMap<String, Integer> requestCodeMap = new HashMap<String, Integer>() {{
        put(Manifest.permission.READ_EXTERNAL_STORAGE, 1);
        put(Manifest.permission.ACCESS_FINE_LOCATION, 2);
    }};

    /**
     * Checks if we have some permission.
     * @param permissionId the permission to be checked, a {@link String} from {@link Manifest.permission}
     * @return true if the permission is granted, false if not
     */
    static boolean checkPermission(Activity activity, String permissionId) {
        int result = ContextCompat.checkSelfPermission(activity,permissionId);
        return (result == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Displays request dialog for permission.
     * @param permissionId the permission requested, a {@link String} from {@link Manifest.permission}
     */
    static void requestPermission(Fragment fragment, String permissionId) {
        fragment.requestPermissions(new String[]{permissionId},requestCodeMap.get(permissionId));
    }
}
