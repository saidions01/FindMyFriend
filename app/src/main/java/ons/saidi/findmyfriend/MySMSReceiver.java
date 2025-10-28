package ons.saidi.findmyfriend;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MySMSReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "FindMyFreinds_ChannelID";
    private static final String CHANNEL_NAME = "FindMyFreinds Location Notifications";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String messageBody, phoneNumber;

        if (intent != null && intent.getAction() != null &&
                intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = bundle.getString("format");

                if (pdus != null && pdus.length > 0) {
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    }

                    if (messages[0] != null) {
                        messageBody = messages[0].getMessageBody();
                        phoneNumber = messages[0].getDisplayOriginatingAddress();

                        Toast.makeText(context,
                                        "Message : " + messageBody + " Reçu de la part de: " + phoneNumber,
                                        Toast.LENGTH_LONG)
                                .show();

                        if(messageBody.startsWith("FindMyFriends: Envoyer moi votre position")) {
                            Intent i = new Intent(context, MyGpsLocationService.class);
                            i.putExtra("sender", phoneNumber);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(i);
                            } else {
                                context.startService(i);
                            }

                        }

                        if(messageBody.startsWith("FindMyFriends: Ma position est")){
                            String[] parts = messageBody.split("#");
                            String longitude = "";
                            String latitude = "";

                            if(parts.length >= 3) {
                                latitude = parts[1];
                                longitude = parts[2];

                                Log.d("MySMSReceiver", "Received location - Lat: " + latitude + ", Long: " + longitude);
                                showLocationNotification(context, latitude, longitude);
                            }
                        }
                    }
                }
            }
        }
    }

    private void showLocationNotification(Context context, String latitude, String longitude) {
        createNotificationChannel(context);

        Intent mapIntent = new Intent(context, MapsActivity.class);
        mapIntent.putExtra("latitude", latitude);
        mapIntent.putExtra("longitude", longitude);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Position Reçue")
                .setContentText("Appuyez pour voir la position sur la carte")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d("MySMSReceiver", "Notification sent successfully");
        } catch (SecurityException e) {
            Log.e("MySMSReceiver", "Notification permission denied", e);
            Toast.makeText(context, "Notification permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for location sharing");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}