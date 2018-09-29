package com.example.ishaan.cohum;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class trackerProfile extends AppCompatActivity implements View.OnClickListener {
    //firebase auth object

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //view objects
    private TextView textViewUserEmail;
    private Button buttonTrackee;
    private Button buttonTracker;

    private static final int PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_profile);

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        Boolean isFirstRun = getSharedPreferences("PREFERENCE3", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            //show sign up activity
            startActivity(new Intent(this, zInformation.class).putExtra("string", "tracking"));
        }
        getSharedPreferences("PREFERENCE3", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();


        //getting current user
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //initializing views
        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);
        buttonTrackee = (Button) findViewById(R.id.buttonTrackee);
        buttonTracker = (Button) findViewById(R.id.buttonTracker);

        //displaying logged in user name
        textViewUserEmail.setText("Click on a button to continue.");

        //adding listener to button
        buttonTrackee.setOnClickListener(this);
        buttonTracker.setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {

        if (view == findViewById(R.id.buttonTrackee)) {
            // Check GPS is enabled
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);


            // Check location permission is granted - if it is, start
            // the service, otherwise request the permission
            int permission = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (permission == PackageManager.PERMISSION_GRANTED) {
                if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    finish();
                    Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
                } else {
                    startTrackerService();
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST);
            }
        }

        if (view == findViewById(R.id.buttonTracker)) {
            startActivity(new Intent(trackerProfile.this, trackerDisplay.class));
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startTrackerService() {
        startService(new Intent(this, trackerService.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start the service when the permission is granted
            startTrackerService();
        } else {
            finish();
        }
    }
}