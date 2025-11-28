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
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.HashMap;
import org.json.JSONObject;


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
        String lower = messageBody.toLowerCase();
        if (messageBody.contains("FindMyFriends: Envoyer moi votre position")
                || lower.contains("send me your location")
                || lower.contains("send me your position")) {
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
            double lon = 0.0, lat = 0.0;
            boolean parsed = false;

            // Format 1: Legacy format with # separator
            String[] t = messageBody.split("#");
            if (t.length >= 3) {
                try {
                    lon = Double.parseDouble(t[1].trim());
                    lat = Double.parseDouble(t[2].trim());
                    parsed = true;
                } catch (NumberFormatException ignored) { }
            }

            // Format 2: Human readable format
            if (!parsed) {
                try {
                    String lowerMsg = messageBody.toLowerCase();
                    int lonIdx = lowerMsg.indexOf("longitude:");
                    int latIdx = lowerMsg.indexOf("latitude:");

                    if (lonIdx != -1 && latIdx != -1) {
                        // Extract longitude part
                        String lonPart = messageBody.substring(lonIdx + "longitude:".length(), latIdx)
                                .replace(",", "").trim();
                        // Extract latitude part (from latitude to end)
                        String latPart = messageBody.substring(latIdx + "latitude:".length())
                                .split("[,\\s]")[0].trim(); // Take first part before comma or space

                        lon = Double.parseDouble(lonPart);
                        lat = Double.parseDouble(latPart);
                        parsed = true;
                    }
                } catch (Exception e) {
                    Log.e("SMSReceiver", "Error parsing coordinates: " + e.getMessage());
                }
            }

            // Format 3: Try to extract numbers directly
            if (!parsed) {
                try {
                    String[] words = messageBody.split("\\s+");
                    for (int i = 0; i < words.length - 1; i++) {
                        if (words[i].equals("longitude:") || words[i].equals("longitude")) {
                            lon = Double.parseDouble(words[i + 1].replace(",", ""));
                        }
                        if (words[i].equals("latitude:") || words[i].equals("latitude")) {
                            lat = Double.parseDouble(words[i + 1].replace(",", ""));
                        }
                    }
                    if (lon != 0.0 && lat != 0.0) {
                        parsed = true;
                    }
                } catch (Exception ignored) { }
            }

            if (parsed && isValidCoordinate(lat) && isValidCoordinate(lon)) {
                showLocationNotification(context, lat, lon, phoneNumber);
            } else {
                Log.e("SMSReceiver", "Failed to parse valid coordinates from: " + messageBody);
            }
        }
    }

    // Move these methods outside of onReceive method
    private boolean isValidCoordinate(double coord) {
        return coord >= -180 && coord <= 180;
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void showLocationNotification(Context context, double lat, double lon, String phoneNumber) {
        try {
            Intent mapIntent = new Intent(context, MapsActivity.class);
            mapIntent.putExtra("longitude", lon);
            mapIntent.putExtra("latitude", lat);
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pi = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), mapIntent, flags);

            // Ensure notification channel exists
            createNotificationChannel(context);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "FindMyFriends_ChannelID")
                    .setContentTitle("📍 Friend Location Received")
                    .setContentText("Tap to view location on map")
                    .setSmallIcon(android.R.drawable.ic_dialog_map)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int) System.currentTimeMillis(), notification.build());

            // Upload to server
            uploadToServer(context, phoneNumber, lat, lon);

        } catch (Exception e) {
            Log.e("SMSReceiver", "Error showing notification: " + e.getMessage());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "FindMyFriends_ChannelID",
                    "FindMyFriends Location Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for location sharing");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void uploadToServer(Context context, String phoneNumber, double lat, double lon) {
        new Thread(() -> {
            try {
                HashMap<String, String> data = new HashMap<>();
                data.put("pseudo", "SMS_Friend");
                data.put("numero", phoneNumber);
                data.put("longitude", String.valueOf(lon));
                data.put("latitude", String.valueOf(lat));

                JSONParser parser = new JSONParser();
                JSONObject response = parser.makeHttpRequest(Config.url_add_position, "POST", data);

                if (response != null && response.getInt("success") == 1) {
                    Log.d("SMSReceiver", "Location uploaded to server successfully");
                }
            } catch (Exception e) {
                Log.e("SMSReceiver", "Error uploading to server: " + e.getMessage());
            }
        }).start();
    }
}