package dev.m1hackers.funtimes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Hashtable;


/** A Fragment for displaying the map with the POI
 */
public class DisplayMapFragment extends Fragment  implements OnMapReadyCallback {

    private static final Hashtable<String, Integer> requestCodeMap = new Hashtable<String, Integer>() {{
        put(Manifest.permission.ACCESS_FINE_LOCATION, 2);
    }};

    private GoogleMap mMap;

    public DisplayMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DisplayMapFragment","DisplayMapFragment onCreate method executing.");

        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            Log.d("permission","Storage permission granted");
        } else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_display_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
    /**
     * Checks if we have some permission.
     * @param permissionId the permission to be checked, a {@link String} from {@link Manifest.permission}
     * @return true if the permission is granted, false if not
     */
    private boolean checkPermission(String permissionId) {
        int result = ContextCompat.checkSelfPermission(DisplayMapFragment.this.getActivity(),permissionId);
        return (result == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Displays request dialog for permission.
     * @param permissionId the permission requested, a {@link String} from {@link Manifest.permission}
     */
    private void requestPermission(String permissionId) {
        ActivityCompat.requestPermissions(this.getActivity(), new String[]{permissionId},
                requestCodeMap.get(permissionId));
    }
}
