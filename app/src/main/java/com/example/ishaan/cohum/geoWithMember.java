package com.example.ishaan.cohum;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.ishaan.cohum.geoTrasitionService.GEOFENCE_NOTIFICATION_ID;

public class geoWithMember extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {

    private static final String TAG = geoWithMember.class.getSimpleName();

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private TextView textLat, textLong;
    private EditText center, radius, geo_user;
    private Spinner spin;
    private String str, creator, name, value, user2, id, str_actual = null, str_check, path;
    private MapFragment mapFragment;
    private float radi;
    private DataSnapshot centertxt;
    private DatabaseReference ref2;
    private int flag;

    private List<String> mChildIds = new ArrayList<>();
    private List<String> mChildNames = new ArrayList<>();

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    private ProgressDialog progressDialog;

    // Create a Intent send by the com.example.ishaan.cohum.notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, geoWithMember.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();

        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            //show sign up activity
            startActivity(new Intent(this, zInformation.class).putExtra("string", "geofence"));
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        path = "users" + "/" + user.getUid() + "/" + "lastLocation";

        textLat = (TextView) findViewById(R.id.lat);
        textLong = (TextView) findViewById(R.id.lon);

        // initialize GoogleMaps
        initGMaps();
        ref2 = mDatabase.child("geofence").child(user.getUid()).child("center");
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren())
                    flag = 1;
                else
                    flag = 0;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (mChildNames.isEmpty()) {
            String userId = user.getUid();
            final Query parentReference = mDatabase.child("users").child(userId).child("members").orderByChild("name");
            //DatabaseReference parentReference = mDatabase.child("users").child(userId).child("members");
            parentReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String s;
                    for (DataSnapshot childId : dataSnapshot.getChildren()) {
                        s = childId.toString();
                        mChildIds.add(s.substring(s.indexOf('=') + 2, s.indexOf(',')));
                        Log.d(TAG, s.substring(s.indexOf('=') + 2, s.indexOf(',')));
                        for (DataSnapshot snap : childId.getChildren()) {
                            if (snap.getKey().equals("name")) {
                                mChildNames.add(snap.getValue().toString());
                                Log.d(TAG, snap.getValue().toString());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(geoWithMember.this, "Failed to load child",
                            Toast.LENGTH_SHORT).show();
                }
            });

        }
        final Handler handler = new Handler();
        final Handler handler1 = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        createSpinner();
                    }
                }, 550);
            }
        }, 1000);
        createGoogleApi();

    }

    private void createSpinner() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompts_spin, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        spin = (Spinner) promptsView.findViewById(R.id.geo_spin2);
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mChildNames);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(dataAdapter);
        alertDialogBuilder
                .setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        user2 = spin.getSelectedItem().toString();
                        mDatabase.child("geofence").child(user.getUid()).child("geo_user").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                if (dataSnapshot.getKey().equals(user2)) {
                                    checkChildGeofence2(user2);
                                } else {
                                    checkChildGeofence(1);
                                }
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finish();
                            }
                        });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void checkChildGeofence2(final String user3) {
        map.clear();
        mDatabase.child("geofence").child(user.getUid()).child("geo_user").child(user2).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String s;
                if (dataSnapshot.getKey().equals(user3)) {
                    s = dataSnapshot.toString();
                    id = s.substring(s.indexOf("{id"), s.indexOf(", enter"));
                    id = id.substring(id.indexOf('='));
                    id = id.replaceAll("[^\\w\\s]", "");
                    checkChildGeofenceStatus();
                    mDatabase.child("users").child(id).child("lastLocation").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            double lat = 0, lng = 0;
                            for (DataSnapshot snap : dataSnapshot.getChildren()) {
                                if (snap.getKey().equals("latitude")) {
                                    lat = Double.parseDouble(snap.getValue().toString());
                                }
                                if (snap.getKey().equals("longitude")) {
                                    lng = Double.parseDouble(snap.getValue().toString());
                                }
                            }


                            LatLng location = new LatLng(lat, lng);
                            map.addMarker(new MarkerOptions().position(location).title(user3)).showInfoWindow();
                            CameraUpdate l = CameraUpdateFactory.newLatLngZoom(location, 15);
                            map.animateCamera(l);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    mDatabase.child("geofence").child(id).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            float radd = radi;
                            for (int i = 0; i < 3; i++) {
                                if (dataSnapshot.getKey().equals("radius")) {
                                    radi = Float.parseFloat(dataSnapshot.getValue().toString());
                                }
                                if (dataSnapshot.getKey().equals("center")) {
                                    centertxt = dataSnapshot;
                                }
                                if (dataSnapshot.getKey().equals("creatorID")) {
                                    creator = dataSnapshot.getValue().toString();
                                }
                                mDatabase.child("users").child(id).addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        if (dataSnapshot.getKey().equals("lastLocation")) {
                                            displayMarker(dataSnapshot);
                                        }
                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            if (radi == 0.0 || radi == radd) {

                            } else {
                                setMarker(radi, centertxt, 0);
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayMarker(DataSnapshot dataSnapshot) {
        double lat = 0, lng = 0;
        for (DataSnapshot snap : dataSnapshot.getChildren()) {
            if (snap.getKey().equals("latitude")) {
                lat = Double.parseDouble(snap.getValue().toString());
            }
            if (snap.getKey().equals("longitude")) {
                lng = Double.parseDouble(snap.getValue().toString());
            }
        }
        LatLng location = new LatLng(lat, lng);
        map.addMarker(new MarkerOptions().position(location).title(user2).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))).showInfoWindow();
    }


    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void set() {
        if (flag == 1) {
            ref2.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    float radd = radi;
                    for (int i = 0; i < 3; i++) {
                        if (dataSnapshot.getKey().equals("radius")) {
                            radi = Float.parseFloat(dataSnapshot.getValue().toString());
                        }
                        if (dataSnapshot.getKey().equals("center")) {
                            centertxt = dataSnapshot;
                        }
                        if (dataSnapshot.getKey().equals("creatorID")) {
                            creator = dataSnapshot.getValue().toString();
                        }

                    }
                    if (radi == 0.0 || radi == radd) {

                    } else {
                        setMarker(radi, centertxt, 1);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            //requestLocationUpdates();
        } else {
            checkChildGeofenceStatus();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        //checkChildGeofence(mDatabase.child("geofence"));
        set();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        switch (item.getItemId()) {
            case R.id.geofence: {
                displayAlert();
                return true;
            }
            case R.id.clear: {
                clearGeofence();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayAlert() {

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        center = (EditText) promptsView.findViewById(R.id.geo_center);
        radius = (EditText) promptsView.findViewById(R.id.geo_radius);
        geo_user = (EditText) promptsView.findViewById(R.id.geo_user);
        if (geoMarker == null) {
            Toast.makeText(this, "Please select the center for the geofence.", Toast.LENGTH_SHORT).show();
        } else {
            LatLng l = geoMarker.getPosition();
            center.setText("Latitude: " + String.valueOf(l.latitude) + " Longitude: " + String.valueOf(l.longitude));
            geo_user.setText(user2);

            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    final String rad = radius.getText().toString();
                                    DatabaseReference parentReference = mDatabase.child("users");
                                    parentReference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String s;
                                            for (DataSnapshot childId : dataSnapshot.getChildren()) {
                                                s = childId.toString();
                                                for (DataSnapshot snap : childId.getChildren()) {
                                                    if (snap.getValue().equals(user2)) {
                                                        str = s.substring(s.indexOf('=') + 2, s.indexOf(','));
                                                        create(str, rad);

                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Toast.makeText(geoWithMember.this, "Failed to load child",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }

    private void create(final String str3, final String rad) {
        if (geoFenceLimits != null)
            geoFenceLimits.remove();
        LatLng l = new LatLng(geoMarker.getPosition().latitude, geoMarker.getPosition().longitude);
        CircleOptions circleOptions = new CircleOptions()
                .center(l)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(Float.parseFloat(rad));
        geoFenceLimits = map.addCircle(circleOptions);

        final DatabaseReference ref2 = mDatabase.child("geofence").child(str3);
        mDatabase.child("users").child(user.getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                creator = dataSnapshot.getValue().toString();
                ref2.child("creatorName").setValue(creator);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase.child("users").child(user.getUid()).child("members").child(str3).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDatabase.child("geofence").child(user.getUid()).child("geo_user").child(dataSnapshot.getValue().toString()).child("id").setValue(str);
                mDatabase.child("geofence").child(user.getUid()).child("geo_user").child(dataSnapshot.getValue().toString()).child("enter").setValue("default");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ref2.child("creatorID").setValue(user.getUid());
        ref2.child("center").setValue(geoMarker.getPosition());
        ref2.child("radius").setValue(rad);
        //saveGeofence(str);
        writeActualLocation(l, 1);
        checkChildGeofence(1);

    }


    private final int REQ_PERMISSION = 999;

    // Check for permission to access Location
    private boolean checkPermission() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.d(TAG, "checkPermission()");
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED);
        }
        return false;
    }


    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        finish();
        Toast.makeText(this, "Enable Location Services", Toast.LENGTH_SHORT).show();
        //TODO close app and warn user
    }

    // Initialize GoogleMaps
    private void initGMaps() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick(" + latLng + ")");
        markerForGeofence(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
        return false;
    }

    private LocationRequest locationRequest;
    // Defined in mili seconds.
// This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL = 1000;
    private final int FASTEST_INTERVAL = 900;

    // Start location Updates
    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged [" + location + "]");
        //lastLocation = location;
        //LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
        //writeActualLocation(l,0);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        if (location != null) {
            Log.d(TAG, "location update " + location);
            ref.child("latitude").setValue(location.getLatitude());
            ref.child("longitude").setValue(location.getLongitude());
        }
        set();
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
        //recoverGeofence();
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                LatLng l = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                //writeActualLocation(l, 0);
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();
    }

    private void writeActualLocation(LatLng location, int flag) {
        if (flag == 1)
            marker2Location(location);
        else
            markerLocation(location);
    }

    private void marker2Location(LatLng latLng) {
        Log.i(TAG, "markerLocation(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        if (locationMarker != null)
            locationMarker.remove();
        locationMarker = map.addMarker(markerOptions);
        float zoom = 16f;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        map.animateCamera(cameraUpdate);

    }

    /*private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }*/

    private Marker locationMarker;

    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation(" + latLng + ")");
        String title = "Geofence Center";
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        if (map != null) {
            if (locationMarker != null)
                locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            float zoom = 16f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }


    private Marker geoMarker;

    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(title);
        if (map != null) {
            // Remove last geoMarker
            if (geoMarker != null)
                geoMarker.remove();
            geoMarker = map.addMarker(markerOptions);

        }
    }

    private void setMarker(float rad, DataSnapshot dataSnapshot, int i) {

        double lat = 0, lng = 0;
        for (DataSnapshot snap : dataSnapshot.getChildren()) {
            if (snap.getKey().equals("latitude")) {
                lat = Double.parseDouble(snap.getValue().toString());
            }
            if (snap.getKey().equals("longitude")) {
                lng = Double.parseDouble(snap.getValue().toString());
            }
        }
        LatLng location = new LatLng(lat, lng);
        if (i == 1) {
            map.clear();
            map.addMarker(new MarkerOptions().position(location).title("Geofence").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))).showInfoWindow();
            startGeofence2(rad, location);
            drawGeofence2(rad, location);
        } else if (i == 0) {
            map.clear();
            map.addMarker(new MarkerOptions().position(location).title("Geofence").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))).showInfoWindow();
            startGeofence(rad, location);
            drawGeofence(rad, location);
        }
        writeActualLocation(location, 0);

    }

    private void drawGeofence2(float rad, LatLng location) {
        Log.d(TAG, "drawGeofence()");

        if (geoFenceLimits2 != null)
            geoFenceLimits2.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(location)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 10, 150, 150))
                .radius(rad);
        geoFenceLimits2 = map.addCircle(circleOptions);

    }

    private void startGeofence2(float rad, LatLng location) {
        if (location != null) {
            //Geofence geofence = createGeofence( geoMarker.getPosition(), GEOFENCE_RADIUS );
            Geofence geofence = createGeofence(location, rad);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }

    }

    // Start Geofence creation process
    private void startGeofence(float rad, LatLng latLng) {
        if (latLng != null) {
            //Geofence geofence = createGeofence( geoMarker.getPosition(), GEOFENCE_RADIUS );
            Geofence geofence = createGeofence(latLng, rad);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }

    }

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
//private static final float GEOFENCE_RADIUS = 500.0f; // in meters

// Create a Geofence

    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d(TAG, "createGeofence");
        map.clear();
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private PendingIntent checkChildIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(this, geoTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    //change
    private void checkChildGeofence(int i) {
        if (i == 1) {
            str_check = str_actual;
            mDatabase.child("geofence").child(user.getUid()).child("geo_user").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    name = dataSnapshot.getKey().toString();
                    s = dataSnapshot.getValue().toString();
                    str_actual = s.substring(s.indexOf("enter") + 6, s.indexOf('}'));
                    if (str_actual.equals("true")) {
                        value = "inside";
                    } else if (str_actual.equals("false")) {
                        value = "exiting";
                    } else if (str_actual.equals("default")) {
                        value = "default";
                    }
                    if (name == null || value == null || str_actual.equals(str_check)) {


                    } else {
                        createNotification(name, value);
                    }
                }


                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else
            checkChildGeofenceStatus();

    }

    private void checkChildGeofenceStatus() {
        str_check = str_actual;
        mDatabase.child("geofence").child(user.getUid()).child("geo_user").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                name = dataSnapshot.getKey().toString();
                s = dataSnapshot.getValue().toString();
                String str1 = s.substring(s.indexOf('='), s.indexOf(','));
                str1 = str1.replaceAll("[^\\w\\s]", "");
                if (str1.equals(id)) {
                    str_actual = s.substring(s.indexOf("enter") + 6, s.indexOf('}'));
                    if (str_actual.equals("true")) {
                        value = "inside";
                    } else if (str_actual.equals("false")) {
                        value = "exiting";
                    } else if (str_actual.equals("default")) {
                        value = "default";
                    }
                    if (name == null || value == null || str_actual.equals(str_check)) {


                    } else {
                        createNotification(name, value);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createNotification(String name, String msg) {
        Log.i(TAG, "sendNotification: " + msg + " " + name);

        // Intent to start the current Activity
        Intent notificationIntent = geoWithMember.makeNotificationIntent(getApplicationContext(), msg);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating and sending Notification
        NotificationManager notificatioMng = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificatioMng.notify(GEOFENCE_NOTIFICATION_ID, createNotification(name, msg, notificationPendingIntent));

    }

    // Create com.example.ishaan.cohum.notification
    private Notification createNotification(String name, String msg, PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isChecked = sharedPreferences.getBoolean(getString(R.string.key_vibrate), false);
        String ringtone = sharedPreferences.getString(getString(R.string.key_notifications_new_ringtone), "");

        if (isChecked) {
            notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        } else {

        }

        if (ringtone.isEmpty()) {
            notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
        } else {
            notificationBuilder.setSound(Uri.parse(ringtone));
        }

        if (msg.equals("default")) {
            Intent intent = new Intent(getApplicationContext(), trackerDisplay.class).putExtra(NOTIFICATION_MSG, msg);
            PendingIntent notificationPendingIntent2 = TaskStackBuilder.create(this).addParentStack(MainActivity.class).addNextIntent(intent).getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(notificationPendingIntent2)
                    .setContentTitle("Track " + name)
                    .setContentText("Can't retrieve geofence details.")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Current status of " + name + " is unavailable. Click to track.")
                            .setBigContentTitle("Track " + name)
                            .setSummaryText("Current status unknown."))
            ;
        } else {
            notificationBuilder.setContentText(name + " is " + msg + " the geofence.")
                    .setContentIntent(notificationPendingIntent)
                    .setContentTitle(name)
            ;
        }

        notificationBuilder
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_tracker)
                .setColor(Color.RED);
        return notificationBuilder.build();
    }


    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            //saveGeofence();
            //drawGeofence();
        } else {
            // inform about fail
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits, geoFenceLimits2;

    private void drawGeofence(float rad, LatLng latLng) {
        Log.d(TAG, "drawGeofence()");

        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(rad);
        geoFenceLimits = map.addCircle(circleOptions);
    }

    private String KEY_GEOFENCE_USER_ID;/* = str;*/
    private final String KEY_GEOFENCE_USER_NAME = user2;

    // Saving GeoFence marker with prefs mng
    /*private void saveGeofence(String str) {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        KEY_GEOFENCE_USER_ID = str;
        editor.putString(KEY_GEOFENCE_USER_ID, "hello");
        //editor.putLong(KEY_GEOFENCE_USER_NAME, );
        editor.apply();
    }

    // Recovering last Geofence marker
    private void recoverGeofence() {
        Log.d(TAG, "recoverGeofence");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        if (sharedPref.contains(KEY_GEOFENCE_USER_NAME) || sharedPref.contains(KEY_GEOFENCE_USER_ID)) {
            str = KEY_GEOFENCE_USER_ID;
            /*double lat = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LAT, -1));
            double lon = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LON, -1));
            LatLng latLng = new LatLng(lat, lon);
            markerForGeofence(latLng);
            //drawGeofence();
        }
    }
*/
// Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    // remove drawing
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw() {
        Log.d(TAG, "removeGeofenceDraw()");
        if (geoMarker != null)
            geoMarker.remove();
        if (geoFenceLimits != null)
            geoFenceLimits.remove();
        map.clear();
        str_check = str_actual;
        mDatabase.child("geofence").child(id).removeValue();
        Toast.makeText(this, "Geofence cleared.", Toast.LENGTH_SHORT).show();

    }

    static double mylat = 0, mylong = 0;

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final String path = "users" + "/" + user.getUid() + "/" + "lastLocation";
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            Task<Void> voidTask = client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d(TAG, "location update " + location);
                        mylat = location.getLatitude();
                        mylong = location.getLongitude();
                        ref.child("latitude").setValue(mylat);
                        ref.child("longitude").setValue(mylong);
                    }
                }
            }, null);
        }
    }
}