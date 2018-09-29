package com.example.ishaan.cohum;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


public class trackerService extends Service {

    private static final String TAG = trackerService.class.getSimpleName();
    private FirebaseUser userReference;
    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        userReference = FirebaseAuth.getInstance().getCurrentUser();
        buildNotification();
        loginToFirebase();
    }

    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_map_black_24dp);
        startForeground(1, builder.build());
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped
            unregisterReceiver(stopReceiver);
            stopSelf();
        }
    };

    private void loginToFirebase() {
        requestLocationUpdates();
    }


    static double mylat=0, mylong=0;

    private void requestLocationUpdates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String test=sharedPreferences.getString(getString(R.string.key_upload_freq), "");
        int duration=0;
        if(!(test.equals(""))){
            if (test.equals("0")){
                duration=5;
            }else if (test.equals("1")){
                duration=10;
            }else if (test.equals("2")){
                duration=15;
            }else if (test.equals("3")){
                duration=20;
            }else if (test.equals("4")){
                duration=30;
            }else if (test.equals("5")){
                duration=60;
            }
        }else{
            duration=10;
        }

        long interval=duration*1000;
        long fastestInterval=(duration/2)*1000;

        LocationRequest request = new LocationRequest();
        request.setInterval(interval);
        request.setFastestInterval(fastestInterval);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final String path = "users" + "/" + userReference.getUid()+"/"+"lastLocation";
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