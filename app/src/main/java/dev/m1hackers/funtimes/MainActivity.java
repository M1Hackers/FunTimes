package dev.m1hackers.funtimes;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements WelcomeFragment.OnMapRequestListener {

    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check that the activity is using the layout version with fragment_container
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state, then we don't need to do
            // anything and should return or else we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            WelcomeFragment mWelcomeFragment = new WelcomeFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            mWelcomeFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            Log.v(LOG_TAG,"Starting WelcomeFragment");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mWelcomeFragment).commit();
        }

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
            Log.v(LOG_TAG,"Settings button clicked");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Return point from WelcomeFragment after RequestImageKeywordsTask has been executed. */
    @Override
    public void onMapRequest(ArrayList<String> keywordList) {
        Log.v(LOG_TAG,"Switching to DisplayMapFragment");

        // Create fragment with arguments
        DisplayMapFragment mMapFragment = new DisplayMapFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("keywordList",keywordList);
        mMapFragment.setArguments(args);

        // Switch fragments
        FragmentTransaction mTransaction = getSupportFragmentManager().beginTransaction();
        mTransaction.replace(R.id.fragment_container, mMapFragment);
        mTransaction.addToBackStack(null);
        mTransaction.commit();
    }
}
