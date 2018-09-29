package com.example.ishaan.cohum;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class trackerDisplay extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = trackerDisplay.class.getSimpleName();
    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private List<String> mChildIds = new ArrayList<>();
    private List<String> mChildNames = new ArrayList<>();
    private int mChildIndex = -1;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_display);

        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference parentReference = mDatabase.child("users").child(userId).child("members");
        parentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(trackerDisplay.this, "\t\tMap Ready\nMembers detected: "+dataSnapshot.getChildrenCount(),
                        Toast.LENGTH_SHORT).show();
                String s;
                for (DataSnapshot childId : dataSnapshot.getChildren())
                {
                    s= childId.toString();
                    mChildIds.add(s.substring(s.indexOf('=')+2, s.indexOf(',')));
                    Log.d(TAG, s.substring(s.indexOf('=')+2, s.indexOf(',')));
                    for(DataSnapshot snap : childId.getChildren())
                    {
                        if(snap.getKey().equals("name"))
                        {
                            mChildNames.add(snap.getValue().toString());
                            Log.d(TAG, snap.getValue().toString());
                        }
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(trackerDisplay.this, "Failed to load child",
                        Toast.LENGTH_SHORT).show();
            }
        });
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

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
        Button buttonSelectChild = findViewById(R.id.button_select_child);
        buttonSelectChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String childId;
                mChildIndex++;
                if (mChildIndex < mChildIds.size()) {
                    childId = mChildIds.get(mChildIndex);
                } else {
                    mChildIndex = 0;
                    childId = mChildIds.get(mChildIndex);
                }



                DatabaseReference childReference = mDatabase.child("users").child(childId);
                childReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName){
                        if(dataSnapshot.getKey().equals("lastLocation"))
                        {
                            setMarker(dataSnapshot);
                            Log.d(TAG, dataSnapshot.toString());
                        }


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                        if(dataSnapshot.getKey().equals("lastLocation"))
                        {
                            setMarker(dataSnapshot);
                            Log.d(TAG, dataSnapshot.toString());
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(trackerDisplay.this, "Failed to load location",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private  void setMarker(DataSnapshot dataSnapshot)
    {

        double lat=0, lng=0;
        for (DataSnapshot snap : dataSnapshot.getChildren()) {
            if(snap.getKey().equals("latitude"))
            {
                lat = Double.parseDouble(snap.getValue().toString());
            }
            if(snap.getKey().equals("longitude"))
            {
                lng = Double.parseDouble(snap.getValue().toString());
            }
        }


        LatLng location = new LatLng(lat, lng);
        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(location).title(mChildNames.get(mChildIndex))).showInfoWindow();
        moveToCurrentLocation(location);
    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,8));
        // Zoom in, animating the camera.
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                currentLocation, 15);
        mMap.animateCamera(location);
    }

}