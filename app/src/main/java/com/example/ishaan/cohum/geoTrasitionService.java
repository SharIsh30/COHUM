package com.example.ishaan.cohum;

/**
 * Created by ishaan on 19-Jan-18.
 */

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class geoTrasitionService extends IntentService {

    private static final String TAG = geoTrasitionService.class.getSimpleName();
    private String creator, user;
    public DatabaseReference ref2 = null;
    public static final int GEOFENCE_NOTIFICATION_ID = 0;
    private DatabaseReference mDatabase;

    public geoTrasitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);
            return;
        }

        final int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type is of interest
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Get the geofence that were triggered
            final List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            mDatabase.child("geofence").child(user).child("creatorID").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!(dataSnapshot.getValue() == null)) {
                        creator = dataSnapshot.getValue().toString();
                        Log.i("well", creator);
                        mDatabase.child("users").child(user).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                ref2 = mDatabase.child("geofence").child(creator).child("geo_user").child(dataSnapshot.getValue().toString());
                                sendNotification(getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences, ref2));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        // Send com.example.ishaan.cohum.notification details as a String}
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences, DatabaseReference ref) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesList.add(geofence.getRequestId());
        }

        String status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            status = "Entering ";
            ref.child("enter").setValue(true);
        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            status = "Exiting ";
            ref.child("enter").setValue(false);
        } else {
            ref2.child("enter").setValue(false);
            status = "default";
        }
        return status + TextUtils.join(", ", triggeringGeofencesList);
    }

    private void sendNotification(String msg) {
        Log.i(TAG, "sendNotification: " + msg);

        // Intent to start the main Activity
        Intent notificationIntent = geoWithMember.makeNotificationIntent(
                getApplicationContext(), msg
        );

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent));

    }

    // Create com.example.ishaan.cohum.notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
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

        notificationBuilder
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_tracker)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }


    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}