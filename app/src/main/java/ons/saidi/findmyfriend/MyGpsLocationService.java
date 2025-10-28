package ons.saidi.findmyfriend;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MyGpsLocationService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final String TAG = "MyGpsLocationService";

    public MyGpsLocationService() {
    }
    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, "FindMyFreinds_ChannelID")
                    .setContentTitle("Getting location")
                    .setContentText("Please wait…")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();
            startForeground(1, notification);
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String phoneNumber = intent.getStringExtra("sender");
        Log.d(TAG, "Location request received from: " + phoneNumber);

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Log.e(TAG, "No phone number provided");
            stopSelf();
            return START_NOT_STICKY;
        }

        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            Log.d(TAG, "Location found - Lat: " + latitude + ", Long: " + longitude);
                            sendLocationSMS(phoneNumber, latitude, longitude);
                        } else {
                            Log.w(TAG, "Last known location is null, requesting current location");
                            requestCurrentLocation(locationClient, phoneNumber);
                        }
                        stopSelf();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to get last location", e);
                        requestCurrentLocation(locationClient, phoneNumber);
                    }
                });

        return START_NOT_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void requestCurrentLocation(FusedLocationProviderClient locationClient, String phoneNumber) {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500)
                .setNumUpdates(1);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Log.d(TAG, "Current location found - Lat: " + latitude + ", Long: " + longitude);
                    sendLocationSMS(phoneNumber, latitude, longitude);
                } else {
                    Log.e(TAG, "Unable to get current location");
                    sendErrorSMS(phoneNumber);
                }
                stopSelf();
            }
        };

        locationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    private void sendLocationSMS(String phoneNumber, double latitude, double longitude) {
        try {
            String message = "FindMyFreinds: Ma position est#" + latitude + "#" + longitude;

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            Log.d(TAG, "Location SMS sent successfully to: " + phoneNumber);
            Toast.makeText(this, "Location sent to " + phoneNumber, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS", e);
            Toast.makeText(this, "Failed to send location SMS", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendErrorSMS(String phoneNumber) {
        try {
            String message = "FindMyFreinds: Unable to get current location. Please try again later.";

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            Log.d(TAG, "Error SMS sent to: " + phoneNumber);

        } catch (Exception e) {
            Log.e(TAG, "Failed to send error SMS", e);
        }
    }
}