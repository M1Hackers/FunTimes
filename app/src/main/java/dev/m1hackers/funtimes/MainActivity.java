package dev.m1hackers.funtimes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int storagePermissionRequestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            Log.d("permission","Storage permission granted");
        } else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Set up Spinner
        Spinner imgNumSpinner = findViewById(R.id.img_num_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> imgNumSpinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.img_num_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        imgNumSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imgNumSpinner.setAdapter(imgNumSpinnerAdapter);

        // Handle button press
        Button uploadButton = findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check whether we are installed on Android 6.0+
                if(Build.VERSION.SDK_INT >= 23) {
                    if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Log.d("permission","Storage permission granted");
                    } else {
                        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }

                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Check if we have a certain permission. */
    private boolean checkPermission(String permissionId) {
        int result = ContextCompat.checkSelfPermission(MainActivity.this,permissionId);
        return (result == PackageManager.PERMISSION_GRANTED);
    }

    /* Displays request dialog for permission. */
    private void requestPermission(String permissionId) {
        ActivityCompat.requestPermissions(this, new String[]{permissionId},storagePermissionRequestCode);
    }

    /* Return point for permissions dialog, toasts the result. */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case storagePermissionRequestCode:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(BuildConfig.DEBUG) Toast.makeText(MainActivity.this,
                            getString(R.string.permission_granted_toast), Toast.LENGTH_LONG).show();
                } else {
                    if(BuildConfig.DEBUG) Toast.makeText(MainActivity.this,
                            getString(R.string.permission_denied_toast), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
