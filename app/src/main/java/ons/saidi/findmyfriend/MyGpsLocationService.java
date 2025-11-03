package ons.saidi.findmyfriend;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
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
    private FusedLocationProviderClient mClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        mClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String number = intent.getStringExtra("sender");
        // start as foreground so Android won't kill the service immediately
        startAsForeground();

        // Try last location first
        mClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                sendLocationSMS(number, location);
                stopSelf();
            } else {
                // request a fresh high-accuracy single update
                LocationRequest request = LocationRequest.create();
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                request.setInterval(1000);
                request.setNumUpdates(1);

                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult result) {
                        if (result != null && result.getLastLocation() != null) {
                            Location loc = result.getLastLocation();
                            sendLocationSMS(number, loc);
                        }
                        // remove updates and stop the service
                        if (mClient != null && locationCallback != null) {
                            mClient.removeLocationUpdates(locationCallback);
                        }
                        stopSelf();
                    }
                };

                mClient.requestLocationUpdates(request, locationCallback, getMainLooper());
            }
        }).addOnFailureListener(e -> {
            // if failed, stop service gracefully
            stopSelf();
        });

        return START_NOT_STICKY;
    }

    private void startAsForeground() {
        // create a minimal persistent notification
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(
                this,
                0,
                notifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        Notification notification = new NotificationCompat.Builder(this, "FindMyFriends_ChannelID")
                .setContentTitle("FindMyFriends")
                .setContentText("Obtaining GPS location...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .build();

        startForeground(2, notification);
    }

    private void sendLocationSMS(String number, Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        SmsManager manager = SmsManager.getDefault();
        String body = "FindMyFriends: ma position est#" + longitude + "#" + latitude;
        manager.sendTextMessage(number, null, body, null, null);
    }

    @Override
    public void onDestroy() {
        if (mClient != null && locationCallback != null) {
            mClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}