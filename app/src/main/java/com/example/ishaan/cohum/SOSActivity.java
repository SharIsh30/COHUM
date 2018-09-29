package com.example.ishaan.cohum;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SOSActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    Button btn;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private List<String> mChildIds = new ArrayList<>();
    private List<String> childPhone = new ArrayList<>();

    private static final String TAG = "SOSActivity";
    String[] perms = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.INTERNET"};
    boolean fineAccessAccepted = false, accessCoarseAccepted = false, InternetAccepted = false;
    int permsRequestCode = 200;
    private ProgressDialog progressDialog;

    int flag = 0;
    double lat, log;
    String phone = "";

    int REQUEST_CHECK_SETTINGS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to continue.", Toast.LENGTH_SHORT).show();
            finish();
        }

        else{
            continueExec();
        }
    }

    private void continueExec() {
        Boolean isFirstRun = getSharedPreferences("PREFERENCE4", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            //show sign up activity
            startActivity(new Intent(this, addSOS.class));
        }
        getSharedPreferences("PREFERENCE4", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();

        btn = (Button) findViewById(R.id.buttonSOS);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (mChildIds.size() == 0) {
                    Toast.makeText(SOSActivity.this, "No members found", Toast.LENGTH_SHORT).show();
                } else {
                    sendSMSMessage();
                }
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, permsRequestCode);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("TAG", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("TAG", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().

                            status.startResolutionForResult(SOSActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("TAG", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("TAG", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });


        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference parentReference = mDatabase.child("sos").child(userId).child("members");
        parentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String s;
                for (DataSnapshot childId : dataSnapshot.getChildren()) {
                    s = childId.getKey().toString();
                    mChildIds.add(s);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SOSActivity.this, "Failed to load child",
                        Toast.LENGTH_SHORT).show();
            }
        });

        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();

        int flag2 = 0;
        while (flag2 != 1) {
            flag2 = createList();
        }
        if (flag2 == 1) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(SOSActivity.this, "SOS Module Ready", Toast.LENGTH_SHORT).show();
        }

    }

    private int createList() {
        int a = 0;
        while (a < mChildIds.size()) {
            DatabaseReference parentReference = mDatabase.child("sos").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("members").child(mChildIds.get(a));
            parentReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot childId : dataSnapshot.getChildren()) {
                        if (childId.getKey().equals("phone")) {
                            setPhoneList(childId.getValue().toString());
                            setFlag();
                            Log.d(TAG, "PhoneList:" + phone);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(SOSActivity.this, "Failed to load child",
                            Toast.LENGTH_SHORT).show();
                }
            });
            ++a;
        }
        return 1;
    }

    protected void sendSMSMessage() {

        if (flag == mChildIds.size()) {
            message();
        } else {
            createList();
            Toast.makeText(this, "Click once more to send the message", Toast.LENGTH_SHORT).show();
        }
    }

    private void message() {
        Log.d(TAG, "KETAN:" + phone);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
        {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.setData(Uri.parse("sms:" + phone));
            smsIntent.putExtra("sms_body", "Here is a link to my location. \nhttps://www.google.com/maps/dir/Current+Location/" + lat + "," + log);
            try {
                startActivity(smsIntent);
                phone="";
                finish();
                Log.i("Finished sending SMS...", "");
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(SOSActivity.this,
                        "SMS failed, please try again later.", Toast.LENGTH_SHORT).show();
            }
        } else // For early versions, do what worked for you before.
        {
            Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", new String(phone.substring(0, phone.length() - 1)));
            smsIntent.putExtra("sms_body", "Here is a link to my location. \nhttps://www.google.com/maps/dir/Current+Location/\" + lat + \",\" + log");
            startActivity(smsIntent);
        }
    }


    public void setFlag() {
        flag += 1;
    }

    public void setPhoneList(String number) {
        phone += number + ";";
    }

    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        switch (permsRequestCode) {

            case 200:

                fineAccessAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                accessCoarseAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                InternetAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                break;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation == null) {
            startLocationUpdates();
        }
        if (mLocation != null) {

            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        lat = location.getLatitude();
        log = location.getLongitude();
    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void addUser(View view) {
        startActivity(new Intent(SOSActivity.this,addSOS.class));
    }

    public void sendInternet(View view) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Here is a link to my location. \nhttps://www.google.com/maps/dir/Current+Location/" + lat + "," + log);
        startActivity(sendIntent);
    }
}

