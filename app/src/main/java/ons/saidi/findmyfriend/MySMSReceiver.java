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
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MySMSReceiver extends BroadcastReceiver {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        String format = bundle.containsKey("format") ? bundle.getString("format") : null;
        if (pdus == null) return;

        StringBuilder fullMessage = new StringBuilder();
        String phoneNumber = null;

        for (Object pdu : pdus) {
            SmsMessage sms;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                } else {
                    sms = SmsMessage.createFromPdu((byte[]) pdu);
                }
            } catch (Exception e) {
                // fallback - try without format
                try {
                    sms = SmsMessage.createFromPdu((byte[]) pdu);
                } catch (Exception ex) {
                    continue;
                }
            }
            if (sms == null) continue;
            if (phoneNumber == null) phoneNumber = sms.getDisplayOriginatingAddress();
            fullMessage.append(sms.getMessageBody());
        }

        String messageBody = fullMessage.toString();
        if (messageBody == null || phoneNumber == null) return;

        // If this is a request message: start the location service and reply
        if (messageBody.contains("FindMyFriends: Envoyer moi votre position")) {
            Intent serviceIntent = new Intent(context, MyGpsLocationService.class);
            serviceIntent.putExtra("sender", phoneNumber);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }

        // If this is a position reply
        if (messageBody.startsWith("FindMyFriends: ma position est")) {
            // parse: "FindMyFriends: ma position est#<longitude>#<latitude>"
            String[] t = messageBody.split("#");
            if (t.length >= 3) {
                String lonStr = t[1].trim();
                String latStr = t[2].trim();
                double lon, lat;
                try {
                    lon = Double.parseDouble(lonStr);
                    lat = Double.parseDouble(latStr);
                } catch (NumberFormatException e) {
                    return; // invalid coordinates
                }

                // Build notification with PendingIntent opening MapsActivity with doubles
                Intent mapIntent = new Intent(context, MapsActivity.class);
                mapIntent.putExtra("longitude", lon);
                mapIntent.putExtra("latitude", lat);
                mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    flags |= PendingIntent.FLAG_IMMUTABLE;
                }

                PendingIntent pi = PendingIntent.getActivity(context, 0, mapIntent, flags);

                NotificationCompat.Builder mynotif = new NotificationCompat.Builder(context, "FindMyFriends_ChannelID")
                        .setContentTitle("Position reçue")
                        .setContentText("Appuyer pour ouvrir la position sur la map")
                        .setSmallIcon(android.R.drawable.ic_dialog_map)
                        .setAutoCancel(true)
                        .setContentIntent(pi);

                // Ensure channel exists on O+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (nm != null) {
                        NotificationChannel chan = new NotificationChannel(
                                "FindMyFriends_ChannelID",
                                "FindMyFriends Notifications",
                                NotificationManager.IMPORTANCE_DEFAULT
                        );
                        nm.createNotificationChannel(chan);
                    }
                }

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
                managerCompat.notify(1001, mynotif.build());
            }
        }
    }
}